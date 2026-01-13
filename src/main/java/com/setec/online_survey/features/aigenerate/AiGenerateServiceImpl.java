package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiGenerateServiceImpl implements AiGenerateService {

    private final GoogleGenAiChatModel googleChatModel;
    private final OpenAiChatModel openAiChatModel;

    public AiGenerateServiceImpl(GoogleGenAiChatModel googleChatModel, OpenAiChatModel openAiChatModel) {
        this.googleChatModel = googleChatModel;
        this.openAiChatModel = openAiChatModel;
    }

    @Override
    public List<AiQuestionResponse> generateSurvey(AiGenerateRequest request) {
        // Choose provider: "openai" or "google" (default to google)
        String provider = (request.provider() != null) ? request.provider().toLowerCase() : "google";

        ChatModel selectedModel = provider.equals("openai") ? openAiChatModel : googleChatModel;
        String modelName = determineModelName(provider, request.level());

        log.info("Using provider: {} with model: {}", provider, modelName);

        return executePrompt(request, selectedModel, modelName, provider);
    }

    private List<AiQuestionResponse> executePrompt(AiGenerateRequest request, ChatModel chatModel, String modelName, String provider) {
        // Build the options based on the provider type
        var options = provider.equals("openai")
                ? OpenAiChatOptions.builder().model(modelName).temperature(0.7).build()
                : GoogleGenAiChatOptions.builder().model(modelName).temperature(0.7).build();

        return ChatClient.create(chatModel).prompt()
                .options(options)
                .user(u -> u.text("""
                        # SYSTEM INSTRUCTIONS
                        You are a professional survey creator. Always output valid JSON.
                        If the prompt is in Khmer, provide labels in Khmer.
                        
                        # TASK
                        Create a {type} survey titled '{title}'.
                        Goal: {prompt}
                        Questions: {count}
                        
                        # OUTPUT FORMAT
                        Return only a JSON array of questions.
                        """)
                        .param("type", request.surveyType())
                        .param("title", request.surveyTitle())
                        .param("prompt", request.prompt())
                        .param("count", request.numberOfQuestions()))
                .call()
                .entity(new ParameterizedTypeReference<List<AiQuestionResponse>>() {});
    }

    private String determineModelName(String provider, String level) {
        if (provider.equals("openai")) {
            return switch (level != null ? level.toUpperCase() : "ADVANCE") {
                case "ADVANCE" -> "gpt-4o";
                case "HARD" -> "gpt-4-turbo";
                default -> "gpt-4o-mini";
            };
        } else {
            return switch (level != null ? level.toUpperCase() : "ADVANCE") {
                case "ADVANCE" -> "gemini-3-flash-preview";
                case "HARD" -> "gemini-2.5-flash";
                case "MID" -> "gemini-2.5-flash-lite";
                case "LOW" -> "gemma-3-27b-it";
                default -> "gemma-3-12b-it";
            };
        }
    }
}