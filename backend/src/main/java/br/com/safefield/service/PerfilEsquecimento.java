package br.com.safefield.service;

import br.com.safefield.domain.TipoEPI;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Probabilidade relativa de cada EPI ser esquecido, usada pelo simulador e pelo gerador de histórico.
 * EPIs que se tiram e recolocam ao longo do turno (óculos, protetor, luvas) faltam com mais frequência
 * do que os que ficam o turno todo no corpo (botina).
 */
public final class PerfilEsquecimento {

    private static final Map<TipoEPI, Double> PESOS = new EnumMap<>(Map.of(
            TipoEPI.OCULOS, 3.0, TipoEPI.PROTETOR_AURICULAR, 3.0, TipoEPI.LUVAS, 2.6,
            TipoEPI.COLETE, 2.0, TipoEPI.CAPACETE, 1.6, TipoEPI.RESPIRADOR, 1.4,
            TipoEPI.AVENTAL, 1.4, TipoEPI.MASCARA_SOLDA, 0.9, TipoEPI.CINTO, 1.0, TipoEPI.BOTA, 0.4));

    private PerfilEsquecimento() {}

    /** Sorteia, entre os tipos informados, qual EPI foi esquecido. */
    public static TipoEPI sortear(Set<TipoEPI> candidatos, Random random) {
        double total = candidatos.stream().mapToDouble(t -> PESOS.getOrDefault(t, 1.0)).sum();
        double alvo = random.nextDouble() * total;
        TipoEPI escolhido = null;
        for (TipoEPI t : candidatos) {
            escolhido = t;
            alvo -= PESOS.getOrDefault(t, 1.0);
            if (alvo <= 0) break;
        }
        return escolhido;
    }
}
