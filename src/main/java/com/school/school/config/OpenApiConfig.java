package com.school.school.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("School API")
                        .description("API для управления учениками, оценками, классами, предметами и учителями")
                        .version("v1")
                        .contact(new Contact().name("Команда Нефор Нормис и Скинхед"))
                        .license(new License().name("Apache 2.0"))
                );
    }
}