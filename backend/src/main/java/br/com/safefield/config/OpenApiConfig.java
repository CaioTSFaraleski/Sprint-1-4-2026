package br.com.safefield.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI safeFieldOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeField AI - API")
                        .version("1.0.0")
                        .description("Monitoramento proativo de EPIs e riscos operacionais na Metaindústria. "
                                + "Faça login em POST /api/v1/auth/login e use o token no botão Authorize."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
