package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.*;
import br.com.safefield.domain.*;
import br.com.safefield.repository.AreaIndustrialRepository;
import br.com.safefield.repository.DeteccaoEPIRepository;
import br.com.safefield.repository.OcorrenciaRiscoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Relatório de conformidade por área, setor, período, turno, operador e tipo de EPI (RF13).
 * Os filtros de área/setor valem para detecções e ocorrências; os de tipo de EPI e severidade
 * recortam apenas as ocorrências (RN05: só entram ocorrências registradas e classificadas).
 */
@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final DeteccaoEPIRepository deteccoes;
    private final OcorrenciaRiscoRepository ocorrencias;
    private final AreaIndustrialRepository areas;
    private final Clock clock;

    public RelatorioService(DeteccaoEPIRepository deteccoes, OcorrenciaRiscoRepository ocorrencias,
                            AreaIndustrialRepository areas, Clock clock) {
        this.deteccoes = deteccoes;
        this.ocorrencias = ocorrencias;
        this.areas = areas;
        this.clock = clock;
    }

    public RelatorioDTO gerar(LocalDate inicio, LocalDate fim, Long areaId, String setor, TipoEPI tipoEpi,
                              NivelRisco severidade) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate ate = fim == null ? hoje : fim;
        LocalDate de = inicio == null ? ate.minusDays(29) : inicio;
        if (de.isAfter(ate)) throw new NegocioException("A data inicial deve ser anterior à final.");
        LocalDateTime ini = de.atStartOfDay();
        LocalDateTime fin = ate.plusDays(1).atStartOfDay();

        List<DeteccaoEPI> det = deteccoes.findByDataHoraBetween(ini, fin).stream()
                .filter(d -> areaId == null || d.getArea().getId().equals(areaId))
                .filter(d -> setor == null || setor.isBlank() || d.getArea().getSetor().equalsIgnoreCase(setor))
                .toList();
        List<OcorrenciaRisco> oc = ocorrencias.findByDataAberturaBetween(ini, fin).stream()
                .filter(o -> areaId == null || o.getArea().getId().equals(areaId))
                .filter(o -> setor == null || setor.isBlank() || o.getArea().getSetor().equalsIgnoreCase(setor))
                .filter(o -> tipoEpi == null || o.getEpisAusentes().stream().anyMatch(e -> e.getTipo() == tipoEpi))
                .filter(o -> severidade == null || o.getSeveridade() == severidade)
                .toList();

        long conformes = det.stream().filter(DeteccaoEPI::isConforme).count();

        Map<Long, List<DeteccaoEPI>> detPorArea = det.stream().collect(Collectors.groupingBy(d -> d.getArea().getId()));
        Map<Long, List<OcorrenciaRisco>> ocPorArea = oc.stream().collect(Collectors.groupingBy(o -> o.getArea().getId()));
        List<LinhaAreaDTO> porArea = areas.findAllByOrderByNomeAsc().stream()
                .filter(a -> detPorArea.containsKey(a.getId()) || ocPorArea.containsKey(a.getId()))
                .map(a -> {
                    List<DeteccaoEPI> d = detPorArea.getOrDefault(a.getId(), List.of());
                    List<OcorrenciaRisco> o = ocPorArea.getOrDefault(a.getId(), List.of());
                    return new LinhaAreaDTO(a.getId(), a.getNome(), a.getSetor(), d.size(),
                            Indicadores.taxaConformidade(d), o.size(),
                            o.stream().filter(x -> x.getSeveridade() == NivelRisco.CRITICO).count(),
                            Indicadores.tempoMedioRegularizacaoMin(o));
                })
                .sorted(Comparator.comparingDouble(LinhaAreaDTO::taxaConformidade))
                .toList();

        Map<TipoEPI, Long> porTipo = new EnumMap<>(TipoEPI.class);
        oc.forEach(o -> o.getEpisAusentes().forEach(e -> porTipo.merge(e.getTipo(), 1L, Long::sum)));
        List<LinhaEpiDTO> porEpi = porTipo.entrySet().stream()
                .sorted(Map.Entry.<TipoEPI, Long>comparingByValue().reversed())
                .map(e -> new LinhaEpiDTO(e.getKey(), e.getKey().getDescricao(), e.getValue(),
                        oc.isEmpty() ? 0 : Indicadores.arredondar(100.0 * e.getValue() / oc.size())))
                .toList();

        Map<Turno, List<DeteccaoEPI>> detPorTurno = det.stream()
                .collect(Collectors.groupingBy(d -> Turno.doHorario(d.getDataHora().toLocalTime())));
        Map<Turno, Long> ocPorTurno = oc.stream()
                .collect(Collectors.groupingBy(o -> Turno.doHorario(o.getDataAbertura().toLocalTime()), Collectors.counting()));
        List<LinhaTurnoDTO> porTurno = Arrays.stream(Turno.values())
                .map(t -> new LinhaTurnoDTO(t, detPorTurno.getOrDefault(t, List.of()).size(),
                        Indicadores.taxaConformidade(detPorTurno.getOrDefault(t, List.of())), ocPorTurno.getOrDefault(t, 0L)))
                .toList();

        List<ReincidenteDTO> reincidentes = oc.stream()
                .collect(Collectors.groupingBy(o -> o.getOperador().getId())).values().stream()
                .filter(l -> l.size() > 1)
                .map(l -> {
                    OcorrenciaRisco o = l.get(0);
                    return new ReincidenteDTO(o.getOperador().getId(), o.getOperador().getNome(),
                            o.getOperador().getMatricula(), o.getArea().getNome(), l.size());
                })
                .sorted(Comparator.comparingLong(ReincidenteDTO::ocorrencias).reversed())
                .limit(10).toList();

        return new RelatorioDTO(de, ate, areaId, setor, tipoEpi, severidade, det.size(), conformes,
                Indicadores.taxaConformidade(det), oc.size(),
                oc.stream().filter(o -> o.getSeveridade() == NivelRisco.CRITICO).count(),
                Indicadores.tempoMedioRegularizacaoMin(oc), Indicadores.taxaReincidencia(oc),
                Indicadores.serieDiaria(de, ate, det, oc), porArea, porEpi, porTurno, reincidentes,
                LocalDateTime.now(clock));
    }

    /** Exportação em CSV (separador ";" e BOM UTF-8, para abrir direto no Excel em português). */
    public String csv(RelatorioDTO r) {
        DateTimeFormatter br = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Relatório de Conformidade - SafeField AI - Metaindústria\n");
        sb.append("Período;").append(r.inicio().format(br)).append(" a ").append(r.fim().format(br)).append('\n');
        sb.append("Gerado em;").append(r.geradoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        sb.append("Indicador;Valor\n");
        sb.append("Detecções analisadas;").append(r.totalDeteccoes()).append('\n');
        sb.append("Índice de conformidade (%);").append(num(r.indiceConformidade())).append('\n');
        sb.append("Ocorrências;").append(r.totalOcorrencias()).append('\n');
        sb.append("Ocorrências críticas;").append(r.ocorrenciasCriticas()).append('\n');
        sb.append("Tempo médio de regularização (min);")
                .append(r.tempoMedioRegularizacaoMin() == null ? "-" : num(r.tempoMedioRegularizacaoMin())).append('\n');
        sb.append("Taxa de reincidência (%);").append(num(r.taxaReincidencia())).append("\n\n");

        sb.append("Área;Setor;Detecções;Conformidade (%);Ocorrências;Críticas;Tempo médio (min)\n");
        for (LinhaAreaDTO a : r.porArea()) {
            sb.append(a.area()).append(';').append(a.setor()).append(';').append(a.deteccoes()).append(';')
                    .append(num(a.taxaConformidade())).append(';').append(a.ocorrencias()).append(';')
                    .append(a.criticas()).append(';')
                    .append(a.tempoMedioRegularizacaoMin() == null ? "-" : num(a.tempoMedioRegularizacaoMin()))
                    .append('\n');
        }
        sb.append("\nEPI ausente;Ocorrências;Percentual (%)\n");
        for (LinhaEpiDTO e : r.porEpi()) {
            sb.append(e.descricao()).append(';').append(e.ocorrencias()).append(';').append(num(e.percentual())).append('\n');
        }
        sb.append("\nData;Detecções;Conformidade (%);Ocorrências;Críticas\n");
        for (SerieDiaDTO d : r.serieDiaria()) {
            sb.append(d.data().format(br)).append(';').append(d.deteccoes()).append(';')
                    .append(num(d.taxaConformidade())).append(';').append(d.ocorrencias()).append(';')
                    .append(d.criticas()).append('\n');
        }
        return sb.toString();
    }

    private static String num(double v) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", v);
    }
}
