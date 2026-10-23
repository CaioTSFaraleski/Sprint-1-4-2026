package br.com.safefield.domain;

/** Tipos de EPI que o sistema de visão computacional/sensores consegue reconhecer. */
public enum TipoEPI {
    CAPACETE("Capacete"),
    OCULOS("Óculos de proteção"),
    LUVAS("Luvas"),
    PROTETOR_AURICULAR("Protetor auricular"),
    BOTA("Bota de segurança"),
    COLETE("Colete refletivo"),
    MASCARA_SOLDA("Máscara de solda"),
    RESPIRADOR("Respirador"),
    AVENTAL("Avental"),
    CINTO("Cinto paraquedista");

    private final String descricao;

    TipoEPI(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
