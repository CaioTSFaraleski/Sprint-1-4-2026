package br.com.safefield.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "safefield")
public record SafeFieldProperties(Jwt jwt, Gateway gateway, Simulador simulador, Seed seed, Cors cors) {

    public record Jwt(String segredo, int validadeHoras) {}

    public record Gateway(String apiKey) {}

    public record Simulador(boolean ativo, long intervaloMs, double probabilidadeNaoConformidade) {}

    public record Seed(boolean ativo, int diasHistorico) {}

    public record Cors(List<String> origens) {}
}
