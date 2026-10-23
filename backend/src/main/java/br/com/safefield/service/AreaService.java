package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.AreaDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.api.dto.Requests.AreaRequest;
import br.com.safefield.api.dto.Requests.RegraRequest;
import br.com.safefield.domain.*;
import br.com.safefield.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Áreas industriais e suas regras de segurança (RF02, RF03, RF04, RN01). */
@Service
@Transactional
public class AreaService {

    private static final EnumSet<StatusOcorrencia> ATIVAS =
            EnumSet.of(StatusOcorrencia.ABERTA, StatusOcorrencia.ESCALADA, StatusOcorrencia.AGUARDANDO_VALIDACAO);

    private final AreaIndustrialRepository areas;
    private final SupervisorRepository supervisores;
    private final EPIRepository epis;
    private final RegraSegurancaRepository regras;
    private final OperadorRepository operadores;
    private final OcorrenciaRiscoRepository ocorrencias;
    private final AuditoriaService auditoria;

    public AreaService(AreaIndustrialRepository areas, SupervisorRepository supervisores, EPIRepository epis,
                       RegraSegurancaRepository regras, OperadorRepository operadores,
                       OcorrenciaRiscoRepository ocorrencias, AuditoriaService auditoria) {
        this.areas = areas;
        this.supervisores = supervisores;
        this.epis = epis;
        this.regras = regras;
        this.operadores = operadores;
        this.ocorrencias = ocorrencias;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<AreaDTO> listar() {
        Map<Long, Long> ativasPorArea = ocorrencias.findByStatusIn(ATIVAS).stream()
                .collect(Collectors.groupingBy(o -> o.getArea().getId(), Collectors.counting()));
        return areas.findAllByOrderByNomeAsc().stream()
                .map(a -> Mapper.area(a, operadores.countByAreaId(a.getId()), ativasPorArea.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaDTO detalhe(Long id) {
        return dto(carregar(id));
    }

    public AreaDTO criar(AreaRequest req, Usuario autor) {
        if (areas.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new NegocioException("Já existe uma área com o nome " + req.nome().trim() + ".");
        }
        AreaIndustrial a = new AreaIndustrial(req.nome().trim(), req.setor().trim(), req.nivelRisco(),
                turnos(req), req.descricao(), supervisor(req.supervisorId()));
        if (req.ativa() != null) a.setAtiva(req.ativa());
        a = areas.save(a);
        auditoria.registrar(TipoEventoAuditoria.AREA_CRIADA, autor, a, null, a.getNivelRisco(),
                "Área " + a.getNome() + " cadastrada (setor " + a.getSetor() + ", risco " + a.getNivelRisco() + ").");
        return dto(a);
    }

    public AreaDTO atualizar(Long id, AreaRequest req, Usuario autor) {
        AreaIndustrial a = carregar(id);
        String nome = req.nome().trim();
        if (!a.getNome().equalsIgnoreCase(nome) && areas.existsByNomeIgnoreCase(nome)) {
            throw new NegocioException("Já existe uma área com o nome " + nome + ".");
        }
        a.setNome(nome);
        a.setSetor(req.setor().trim());
        a.setNivelRisco(req.nivelRisco());
        a.setTurnos(turnos(req));
        a.setDescricao(req.descricao());
        a.setSupervisor(supervisor(req.supervisorId()));
        if (req.ativa() != null) a.setAtiva(req.ativa());
        auditoria.registrar(TipoEventoAuditoria.AREA_ATUALIZADA, autor, a, null, a.getNivelRisco(),
                "Cadastro da área " + a.getNome() + " atualizado" + (a.isAtiva() ? "." : " (área inativa)."));
        return dto(a);
    }

    public AreaDTO adicionarRegra(Long areaId, RegraRequest req, Usuario autor) {
        AreaIndustrial a = carregar(areaId);
        EPI epi = epis.findById(req.epiId()).orElseThrow(() -> new NaoEncontradoException("EPI", req.epiId()));
        String atividade = req.atividade() == null || req.atividade().isBlank() ? null : req.atividade().trim();
        boolean duplicada = a.getRegras().stream().anyMatch(r -> r.getEpi().getTipo() == epi.getTipo()
                && r.getTurno() == req.turno() && Objects.equals(r.getAtividade(), atividade));
        if (duplicada) {
            throw new NegocioException("Já existe uma regra para " + epi.getTipo().getDescricao()
                    + " nesse turno/atividade.");
        }
        RegraSeguranca r = new RegraSeguranca(a, epi, req.descricao().trim(), req.severidadePadrao(), req.turno(),
                atividade);
        a.getRegras().add(regras.save(r));
        auditoria.registrar(TipoEventoAuditoria.REGRA_CRIADA, autor, a, null, req.severidadePadrao(),
                "Regra adicionada: " + r.getDescricao() + " (" + epi.getTipo().getDescricao()
                        + (req.turno() == null ? ", todos os turnos" : ", turno " + req.turno()) + ").");
        return dto(a);
    }

    public AreaDTO removerRegra(Long areaId, Long regraId, Usuario autor) {
        AreaIndustrial a = carregar(areaId);
        RegraSeguranca r = a.getRegras().stream().filter(x -> x.getId().equals(regraId)).findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Regra", regraId));
        a.getRegras().remove(r);
        auditoria.registrar(TipoEventoAuditoria.REGRA_REMOVIDA, autor, a, null, r.getSeveridadePadrao(),
                "Regra removida: " + r.getDescricao() + ".");
        return dto(a);
    }

    private AreaDTO dto(AreaIndustrial a) {
        long ativas = ocorrencias.findByStatusIn(ATIVAS).stream()
                .filter(o -> o.getArea().getId().equals(a.getId())).count();
        return Mapper.area(a, operadores.countByAreaId(a.getId()), ativas);
    }

    private AreaIndustrial carregar(Long id) {
        return areas.findWithRegrasById(id).orElseThrow(() -> new NaoEncontradoException("Área", id));
    }

    private SupervisorSeguranca supervisor(Long id) {
        if (id == null) return null;
        return supervisores.findById(id).orElseThrow(() -> new NaoEncontradoException("Supervisor", id));
    }

    private static String turnos(AreaRequest req) {
        return req.turnos().stream().distinct().sorted().map(Enum::name).collect(Collectors.joining(","));
    }
}
