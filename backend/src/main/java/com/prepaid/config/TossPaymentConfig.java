package com.prepaid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossPaymentConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.url}")
    private String baseUrl;

    @Bean
    public RestClient tossRestClient(RestClient.Builder builder) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encodedSecretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
