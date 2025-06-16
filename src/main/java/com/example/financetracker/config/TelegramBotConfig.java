package com.example.financetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TelegramBotConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }
}