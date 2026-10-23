package br.com.safefield.service;

import br.com.safefield.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Motor de não conformidade (RF06): compara os EPIs detectados com as regras ativas da área
 * no turno da detecção e calcula a severidade do risco.
 *
 * Severidade = maior severidade entre as regras violadas, subindo um nível quando faltam
 * dois ou mais EPIs e mais um nível quando o operador é reincidente (o mesmo EPI já faltou nos últimos 7 dias).
 */
@Service
public class MotorRegrasService {

    public record Avaliacao(List<RegraSeguranca> regrasVioladas, List<EPI> episAusentes, NivelRisco severidade) {
        public boolean conforme() {
            return regrasVioladas.isEmpty();
        }
    }

    public Avaliacao avaliar(AreaIndustrial area, Turno turno, Set<TipoEPI> detectados, boolean reincidente) {
        List<RegraSeguranca> violadas = area.obterRegrasAtivas(turno).stream()
                .filter(r -> !r.avaliar(detectados))
                .toList();
        if (violadas.isEmpty()) return new Avaliacao(List.of(), List.of(), null);

        // um EPI por tipo (duas regras podem exigir o mesmo tipo em atividades diferentes)
        Map<TipoEPI, EPI> ausentes = new LinkedHashMap<>();
        NivelRisco severidade = null;
        for (RegraSeguranca r : violadas) {
            ausentes.putIfAbsent(r.getEpi().getTipo(), r.getEpi());
            severidade = NivelRisco.maior(severidade, r.getSeveridadePadrao());
        }
        if (ausentes.size() >= 2) severidade = severidade.acima();
        if (reincidente) severidade = severidade.acima();
        return new Avaliacao(violadas, List.copyOf(ausentes.values()), severidade);
    }

    /** Tipos de EPI exigidos na área para o turno informado. */
    public Set<TipoEPI> tiposExigidos(AreaIndustrial area, Turno turno) {
        Set<TipoEPI> tipos = EnumSet.noneOf(TipoEPI.class);
        area.obterRegrasAtivas(turno).forEach(r -> tipos.add(r.getEpi().getTipo()));
        return tipos;
    }
}
