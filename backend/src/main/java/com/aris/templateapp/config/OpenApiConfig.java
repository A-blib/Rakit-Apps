package com.aris.templateapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Judul dokumentasi di Swagger UI dan skema "bearerAuth", supaya nanti tombol Authorize
 * bisa diisi access token untuk mencoba endpoint yang butuh login.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI templateAppOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("App Template Website API")
                        .description("Autentikasi, penyambungan akun, onboarding, dan mode.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
