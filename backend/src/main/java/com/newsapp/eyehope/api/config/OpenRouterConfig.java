package com.newsapp.eyehope.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.model}")
    private String model;

    private static final String OPENROUTER_API_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getApiUrl() {
        return OPENROUTER_API_URL;
    }
}
