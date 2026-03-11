package com.ecommers.product_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "C7K4w+RBnJfIr1TZgb/JUWMiASrTq4cIWxkpExNtwxFYIfENDMUOY+RxRZKe4P8Yv7ky6+98bZYP1k6ot5lr5A==";

    @Bean
    public OpenAPI customOpenAPI(){

        return new OpenAPI()

                .info(new Info()
                        .title("Product Service API")
                        .version("1.0")
                        .description("API documentation for product service in E-Commerce System"))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}