package com.logisticsapplication.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticsOpenApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Logistics Application API")
                        .description(
                                "API для управления пользователями, транспортом и отправлениями"
                        )
                        .version("v1")
                        .contact(
                                new Contact()
                                        .name("Logistics Team")
                                        .email("support@logistics.local")
                        )
                        .license(new License().name("Apache 2.0"))
        );
    }
}
