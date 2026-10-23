package br.com.safefield.domain;

/** Severidade de um risco. A ordem importa: usada para comparar e escalar severidades. */
public enum NivelRisco {
    BAIXO, MEDIO, ALTO, CRITICO;

    public NivelRisco acima() {
        return this == CRITICO ? CRITICO : values()[ordinal() + 1];
    }

    public static NivelRisco maior(NivelRisco a, NivelRisco b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
