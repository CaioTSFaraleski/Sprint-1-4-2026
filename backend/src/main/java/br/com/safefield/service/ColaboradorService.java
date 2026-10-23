package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.*;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.*;
import br.com.safefield.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visão "Colaboradores e EPIs" do protótipo: EPIs obrigatórios da área de cada operador,
 * o que está pendente (ocorrências ainda não regularizadas), fichas de entrega e histórico.
 */
@Service
@Transactional(readOnly = true)
public class ColaboradorService {

    private final OperadorRepository operadores;
    private final AreaIndustrialRepository areas;
    private final OcorrenciaRiscoRepository ocorrencias;
    private final DeteccaoEPIRepository deteccoes;
    private final EntregaEPIRepository entregas;
    private final Clock clock;

    public ColaboradorService(OperadorRepository operadores, AreaIndustrialRepository areas,
                              OcorrenciaRiscoRepository ocorrencias, DeteccaoEPIRepository deteccoes,
                              EntregaEPIRepository entregas, Clock clock) {
        this.operadores = operadores;
        this.areas = areas;
        this.ocorrencias = ocorrencias;
        this.deteccoes = deteccoes;
        this.entregas = entregas;
        this.clock = clock;
    }

    public List<ColaboradorResumoDTO> listar(String busca, Long areaId, String situacao) {
        String termo = busca == null ? "" : busca.toLowerCase(Locale.ROOT).trim();
        Map<Long, List<OcorrenciaRisco>> ativasPorOperador = ocorrencias.findByStatusIn(EnumSet.of(
                        StatusOcorrencia.ABERTA, StatusOcorrencia.ESCALADA, StatusOcorrencia.AGUARDANDO_VALIDACAO))
                .stream().collect(Collectors.groupingBy(o -> o.getOperador().getId()));
        return operadores.findAllByOrderByNomeAsc().stream()
                .filter(o -> areaId == null || (o.getArea() != null && o.getArea().getId().equals(areaId)))
                .filter(o -> termo.isEmpty() || o.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || o.getMatricula().toLowerCase(Locale.ROOT).contains(termo)
                        || (o.getArea() != null && (o.getArea().getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || o.getArea().getSetor().toLowerCase(Locale.ROOT).contains(termo))))
                .map(o -> resumo(o, ativasPorOperador.getOrDefault(o.getId(), List.of())))
                .filter(c -> situacao == null || situacao.isBlank() || c.situacao().equalsIgnoreCase(situacao))
                .toList();
    }

    public ColaboradorDetalheDTO detalhe(Long id, Usuario usuario) {
        if (usuario instanceof Operador && !usuario.getId().equals(id)) {
            throw new AcessoNegadoException("O operador só acessa a própria ficha.");
        }
        Operador op = operadores.findById(id).orElseThrow(() -> new NaoEncontradoException("Colaborador", id));
        LocalDate hoje = LocalDate.now(clock);
        List<OcorrenciaRisco> historico = ocorrencias.findByOperadorIdOrderByDataAberturaDesc(id);
        List<OcorrenciaRisco> ativas = historico.stream().filter(o -> o.getStatus().ativa()).toList();
        Set<TipoEPI> pendentes = pendentes(ativas);
        Optional<DeteccaoEPI> ultima = deteccoes.findTopByOperadorIdOrderByDataHoraDesc(id);
        List<EntregaEPI> fichas = entregas.findByOperadorIdOrderByDataEntregaDesc(id);
        Set<TipoEPI> entreguesValidos = fichas.stream().filter(f -> f.statusEm(hoje) == StatusEntrega.ATIVA)
                .map(f -> f.getEpi().getTipo()).collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoEPI.class)));

        List<EpiObrigatorioDTO> obrigatorios = new ArrayList<>();
        if (op.getArea() != null) {
            AreaIndustrial area = areas.findWithRegrasById(op.getArea().getId()).orElseThrow();
            Turno turno = op.getTurno() == null ? Turno.doHorario(LocalDateTime.now(clock).toLocalTime()) : op.getTurno();
            Map<TipoEPI, RegraSeguranca> porTipo = new EnumMap<>(TipoEPI.class);
            for (RegraSeguranca r : area.obterRegrasAtivas(turno)) {
                porTipo.merge(r.getEpi().getTipo(), r, (a, b) ->
                        a.getSeveridadePadrao().ordinal() >= b.getSeveridadePadrao().ordinal() ? a : b);
            }
            porTipo.forEach((tipo, r) -> obrigatorios.add(new EpiObrigatorioDTO(tipo, tipo.getDescricao(),
                    r.getEpi().getNome(), r.getSeveridadePadrao(), ultima.isPresent() && !pendentes.contains(tipo),
                    entreguesValidos.contains(tipo))));
        }

        long recentes = historico.stream()
                .filter(o -> o.getDataAbertura().isAfter(LocalDateTime.now(clock).minusDays(30))).count();
        return new ColaboradorDetalheDTO(resumo(op, ativas), op.getEmail(), obrigatorios,
                fichas.stream().map(f -> Mapper.entrega(f, hoje)).toList(),
                ultima.map(d -> Mapper.deteccao(d, null)).orElse(null),
                historico.stream().limit(20).map(Mapper::ocorrencia).toList(),
                historico.size(), recentes >= 2);
    }

    private ColaboradorResumoDTO resumo(Operador o, List<OcorrenciaRisco> ativas) {
        Set<TipoEPI> pendentes = pendentes(ativas);
        Optional<DeteccaoEPI> ultima = deteccoes.findTopByOperadorIdOrderByDataHoraDesc(o.getId());
        String situacao;
        if (!pendentes.isEmpty()) situacao = "PENDENTE";
        else if (!ativas.isEmpty()) situacao = "EM_VALIDACAO";
        else if (ultima.isPresent()) situacao = "CONFORME";
        else situacao = "SEM_LEITURA";
        AreaIndustrial area = o.getArea();
        return new ColaboradorResumoDTO(o.getId(), o.getNome(), o.getMatricula(), o.getFuncao(), o.getTurno(),
                area == null ? null : area.getId(), area == null ? null : area.getNome(),
                area == null ? null : area.getSetor(), situacao, pendentes.size(), ativas.size(),
                ultima.map(DeteccaoEPI::getDataHora).orElse(null));
    }

    /** EPIs que continuam faltando: os das ocorrências abertas ou escaladas (ainda não regularizadas). */
    private static Set<TipoEPI> pendentes(List<OcorrenciaRisco> ativas) {
        Set<TipoEPI> tipos = EnumSet.noneOf(TipoEPI.class);
        ativas.stream()
                .filter(o -> o.getStatus() == StatusOcorrencia.ABERTA || o.getStatus() == StatusOcorrencia.ESCALADA)
                .forEach(o -> o.getEpisAusentes().forEach(e -> tipos.add(e.getTipo())));
        return tipos;
    }
}
