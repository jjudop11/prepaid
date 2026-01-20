package com.prepaid.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정
 * http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prepaid Platform API")
                        .description("선불관리 플랫폼 REST API 문서")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Prepaid Team")
                                .email("support@prepaid.com")))
                .components(new Components()
                        .addSecuritySchemes("cookie-auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("accessToken")))
                .addSecurityItem(new SecurityRequirement().addList("cookie-auth"));
    }
}
