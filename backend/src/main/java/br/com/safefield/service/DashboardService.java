package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.*;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.*;
import br.com.safefield.repository.AreaIndustrialRepository;
import br.com.safefield.repository.DeteccaoEPIRepository;
import br.com.safefield.repository.OcorrenciaRiscoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Painel de monitoramento do supervisor e do gestor (RF14). */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OcorrenciaRiscoRepository ocorrencias;
    private final DeteccaoEPIRepository deteccoes;
    private final AreaIndustrialRepository areas;
    private final SimuladorService simulador;
    private final Clock clock;

    public DashboardService(OcorrenciaRiscoRepository ocorrencias, DeteccaoEPIRepository deteccoes,
                            AreaIndustrialRepository areas, SimuladorService simulador, Clock clock) {
        this.ocorrencias = ocorrencias;
        this.deteccoes = deteccoes;
        this.areas = areas;
        this.simulador = simulador;
        this.clock = clock;
    }

    public DashboardDTO resumo() {
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDate hoje = agora.toLocalDate();
        LocalDateTime inicio7d = hoje.minusDays(6).atStartOfDay();
        LocalDateTime inicio14d = hoje.minusDays(13).atStartOfDay();

        List<OcorrenciaRisco> ativas = ocorrencias.findByStatusIn(EnumSet.of(StatusOcorrencia.ABERTA,
                StatusOcorrencia.ESCALADA, StatusOcorrencia.AGUARDANDO_VALIDACAO));
        List<DeteccaoEPI> det14 = deteccoes.findByDataHoraBetween(inicio14d, agora.plusMinutes(1));
        List<DeteccaoEPI> det7 = det14.stream().filter(d -> !d.getDataHora().isBefore(inicio7d)).toList();
        List<DeteccaoEPI> detAnterior = det14.stream().filter(d -> d.getDataHora().isBefore(inicio7d)).toList();
        List<DeteccaoEPI> detHoje = det7.stream().filter(d -> d.getDataHora().toLocalDate().equals(hoje)).toList();
        List<OcorrenciaRisco> oc7 = ocorrencias.findByDataAberturaBetween(inicio7d, agora.plusMinutes(1));

        double conf7 = Indicadores.taxaConformidade(det7);
        double confAnterior = Indicadores.taxaConformidade(detAnterior);

        Map<Long, List<DeteccaoEPI>> detPorArea = det7.stream().collect(Collectors.groupingBy(d -> d.getArea().getId()));
        Map<Long, Long> ocPorArea = oc7.stream().collect(Collectors.groupingBy(o -> o.getArea().getId(), Collectors.counting()));
        Map<Long, Long> ativasPorArea = ativas.stream().collect(Collectors.groupingBy(o -> o.getArea().getId(), Collectors.counting()));
        List<AreaRiscoDTO> ranking = areas.findAllByOrderByNomeAsc().stream()
                .filter(AreaIndustrial::isAtiva)
                .map(a -> new AreaRiscoDTO(a.getId(), a.getNome(), a.getSetor(), a.getNivelRisco(),
                        ativasPorArea.getOrDefault(a.getId(), 0L), ocPorArea.getOrDefault(a.getId(), 0L),
                        Indicadores.taxaConformidade(detPorArea.getOrDefault(a.getId(), List.of()))))
                .sorted(Comparator.comparingLong(AreaRiscoDTO::ocorrenciasAtivas).reversed()
                        .thenComparingDouble(AreaRiscoDTO::taxaConformidade7d))
                .toList();

        List<OcorrenciaResumoDTO> listaAtivas = ativas.stream()
                .sorted(Comparator.comparing(OcorrenciaRisco::getSeveridade).reversed()
                        .thenComparing(OcorrenciaRisco::getDataAbertura, Comparator.reverseOrder()))
                .limit(8).map(Mapper::ocorrencia).toList();

        List<DeteccaoEPI> ultimas = deteccoes.findTop15ByOrderByDataHoraDesc();
        Map<Long, OcorrenciaRisco> ocPorDeteccao = ultimas.isEmpty() ? Map.of()
                : ocorrencias.findByDeteccaoIdIn(ultimas.stream().map(DeteccaoEPI::getId).toList()).stream()
                .collect(Collectors.toMap(o -> o.getDeteccao().getId(), Function.identity()));

        return new DashboardDTO(
                ativas.size(),
                ativas.stream().filter(o -> o.getSeveridade() == NivelRisco.CRITICO).count(),
                ativas.stream().filter(o -> o.getStatus() == StatusOcorrencia.AGUARDANDO_VALIDACAO).count(),
                ativas.stream().filter(o -> o.getStatus() == StatusOcorrencia.ESCALADA).count(),
                detHoje.size(),
                Indicadores.taxaConformidade(detHoje),
                conf7,
                Indicadores.arredondar(conf7 - confAnterior),
                Indicadores.tempoMedioRegularizacaoMin(oc7),
                Indicadores.taxaReincidencia(oc7),
                Indicadores.serieDiaria(hoje.minusDays(6), hoje, det7, oc7),
                ranking,
                listaAtivas,
                ultimas.stream().map(d -> Mapper.deteccao(d, ocPorDeteccao.get(d.getId()))).toList(),
                simulador.isAtivo());
    }
}
