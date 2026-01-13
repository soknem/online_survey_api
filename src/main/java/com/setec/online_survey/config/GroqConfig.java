package com.setec.online_survey.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqConfig {

    @Value("${ai.groq.api-key}")
    private String groqApiKey;

    @Value("${ai.groq.base-url}")
    private String groqBaseUrl;

    @Bean
    public OpenAiChatModel groqChatModel() {
        // Correct way to initialize OpenAiApi in Spring AI 2.0.0-M1
        OpenAiApi groqApi = OpenAiApi.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .build();

        // Use the Builder for the ChatModel as well
        return OpenAiChatModel.builder()
                .openAiApi(groqApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("llama-3.3-70b-versatile")
                        .temperature(0.7)
                        .build())
                .build();
    }
}