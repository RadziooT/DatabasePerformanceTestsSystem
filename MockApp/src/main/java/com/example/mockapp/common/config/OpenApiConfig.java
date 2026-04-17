package com.example.mockapp.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mockAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mock App API")
                        .description("REST API for MockApp operations used in DB performance tests")
                        .version("v1.0.0")
                );
    }
}
