package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.EntregaDTO;
import br.com.safefield.api.dto.Dtos.EpiDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.api.dto.Requests.EntregaRequest;
import br.com.safefield.api.dto.Requests.EpiRequest;
import br.com.safefield.domain.*;
import br.com.safefield.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Catálogo de EPIs e fichas de entrega aos colaboradores. */
@Service
@Transactional
public class EpiService {

    private final EPIRepository epis;
    private final EntregaEPIRepository entregas;
    private final UsuarioRepository usuarios;
    private final AreaIndustrialRepository areas;
    private final RegraSegurancaRepository regras;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public EpiService(EPIRepository epis, EntregaEPIRepository entregas, UsuarioRepository usuarios,
                      AreaIndustrialRepository areas, RegraSegurancaRepository regras, AuditoriaService auditoria,
                      Clock clock) {
        this.epis = epis;
        this.entregas = entregas;
        this.usuarios = usuarios;
        this.areas = areas;
        this.regras = regras;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<EpiDTO> listar() {
        LocalDate hoje = LocalDate.now(clock);
        return epis.findAllByOrderByNomeAsc().stream().map(e -> Mapper.epi(e, hoje)).toList();
    }

    public EpiDTO criar(EpiRequest req, Usuario autor) {
        LocalDate hoje = LocalDate.now(clock);
        EPI epi = epis.save(new EPI(req.nome().trim(), req.tipo(), req.codigoCA().trim(), req.validadeCA(),
                req.observacoes()));
        auditoria.registrar(TipoEventoAuditoria.EPI_CADASTRADO, autor, null, null, null,
                "EPI cadastrado: " + epi.getNome() + " (CA " + epi.getCodigoCA() + ", validade "
                        + epi.getValidadeCA() + ").");

        if (req.operadorId() != null) {
            entregar(req.operadorId(), new EntregaRequest(epi.getId(), hoje, null), autor);
        }
        if (req.areaObrigatoriaId() != null) {
            AreaIndustrial area = areas.findWithRegrasById(req.areaObrigatoriaId())
                    .orElseThrow(() -> new NaoEncontradoException("Área", req.areaObrigatoriaId()));
            NivelRisco severidade = req.severidadeObrigatoria() == null ? NivelRisco.MEDIO : req.severidadeObrigatoria();
            boolean jaExigido = area.getRegras().stream()
                    .anyMatch(r -> r.getEpi().getTipo() == epi.getTipo() && r.getTurno() == null && r.getAtividade() == null);
            if (!jaExigido) {
                RegraSeguranca r = regras.save(new RegraSeguranca(area, epi,
                        "Uso obrigatório de " + epi.getNome() + " em " + area.getNome(), severidade, null, null));
                area.getRegras().add(r);
                auditoria.registrar(TipoEventoAuditoria.REGRA_CRIADA, autor, area, null, severidade,
                        "Regra adicionada: " + r.getDescricao() + ".");
            }
        }
        return Mapper.epi(epi, hoje);
    }

    public EntregaDTO entregar(Long operadorId, EntregaRequest req, Usuario autor) {
        LocalDate hoje = LocalDate.now(clock);
        Operador operador = usuarios.findById(operadorId).filter(Operador.class::isInstance).map(Operador.class::cast)
                .orElseThrow(() -> new NaoEncontradoException("Operador", operadorId));
        EPI epi = epis.findById(req.epiId()).orElseThrow(() -> new NaoEncontradoException("EPI", req.epiId()));
        if (!epi.validarCertificacao(hoje)) {
            throw new NegocioException("O CA " + epi.getCodigoCA() + " de " + epi.getNome()
                    + " está vencido. Não é permitido entregar EPI sem certificado válido.");
        }
        LocalDate dataEntrega = req.dataEntrega() == null ? hoje : req.dataEntrega();
        LocalDate validade = req.dataValidade() != null ? req.dataValidade() : dataEntrega.plusMonths(6);
        if (validade.isAfter(epi.getValidadeCA())) validade = epi.getValidadeCA();
        EntregaEPI entrega = entregas.save(new EntregaEPI(operador, epi, dataEntrega, validade));
        auditoria.registrar(TipoEventoAuditoria.EPI_ENTREGUE, autor, null, null, null,
                epi.getNome() + " entregue a " + operador.getNome() + " (válido até " + validade + ").");
        return Mapper.entrega(entrega, hoje);
    }
}
