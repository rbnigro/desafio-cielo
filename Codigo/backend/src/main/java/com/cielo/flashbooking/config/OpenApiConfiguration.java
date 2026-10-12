package com.cielo.flashbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI flashBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flash Booking API")
                        .version("1.0.0")
                        .description("API para eventos e reservas temporarias de ingressos."));
    }
}
