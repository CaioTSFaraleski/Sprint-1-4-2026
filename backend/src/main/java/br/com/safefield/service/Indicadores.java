package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.SerieDiaDTO;
import br.com.safefield.domain.DeteccaoEPI;
import br.com.safefield.domain.NivelRisco;
import br.com.safefield.domain.OcorrenciaRisco;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Cálculos de indicadores de conformidade compartilhados pelo dashboard e pelos relatórios (RF13 / RF14). */
final class Indicadores {

    private Indicadores() {}

    /** Percentual de detecções em que todos os EPIs obrigatórios estavam presentes. */
    static double taxaConformidade(Collection<DeteccaoEPI> deteccoes) {
        if (deteccoes.isEmpty()) return 100.0;
        long conformes = deteccoes.stream().filter(DeteccaoEPI::isConforme).count();
        return arredondar(100.0 * conformes / deteccoes.size());
    }

    /** Minutos médios entre a abertura da ocorrência e a regularização registrada pelo operador. */
    static Double tempoMedioRegularizacaoMin(Collection<OcorrenciaRisco> ocorrencias) {
        OptionalDouble media = ocorrencias.stream()
                .filter(o -> o.getDataRegularizacao() != null)
                .mapToLong(o -> Duration.between(o.getDataAbertura(), o.getDataRegularizacao()).toSeconds())
                .average();
        return media.isPresent() ? arredondar(media.getAsDouble() / 60.0) : null;
    }

    /**
     * Percentual de ocorrências reincidentes: o mesmo EPI já tinha faltado para o operador nos 7 dias
     * anteriores (mesmo critério que o motor de regras usa para elevar a severidade).
     */
    static double taxaReincidencia(Collection<OcorrenciaRisco> ocorrencias) {
        if (ocorrencias.isEmpty()) return 0.0;
        long reincidentes = ocorrencias.stream().filter(OcorrenciaRisco::isReincidente).count();
        return arredondar(100.0 * reincidentes / ocorrencias.size());
    }

    static List<SerieDiaDTO> serieDiaria(LocalDate inicio, LocalDate fim, Collection<DeteccaoEPI> deteccoes,
                                         Collection<OcorrenciaRisco> ocorrencias) {
        Map<LocalDate, List<DeteccaoEPI>> detPorDia = deteccoes.stream()
                .collect(Collectors.groupingBy(d -> d.getDataHora().toLocalDate()));
        Map<LocalDate, List<OcorrenciaRisco>> ocPorDia = ocorrencias.stream()
                .collect(Collectors.groupingBy(o -> o.getDataAbertura().toLocalDate()));
        List<SerieDiaDTO> serie = new ArrayList<>();
        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            List<DeteccaoEPI> det = detPorDia.getOrDefault(dia, List.of());
            List<OcorrenciaRisco> oc = ocPorDia.getOrDefault(dia, List.of());
            long conformes = det.stream().filter(DeteccaoEPI::isConforme).count();
            serie.add(new SerieDiaDTO(dia, det.size(), conformes, taxaConformidade(det), oc.size(),
                    oc.stream().filter(o -> o.getSeveridade() == NivelRisco.CRITICO).count()));
        }
        return serie;
    }

    static double arredondar(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
