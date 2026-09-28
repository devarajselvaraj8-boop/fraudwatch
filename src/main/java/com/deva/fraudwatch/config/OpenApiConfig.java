package com.deva.fraudwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudWatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FraudWatch API")
                        .description("FraudWatch - Basic Transaction Anomaly Flagging System REST APIs")
                        .version("1.0.0"));
    }
}
