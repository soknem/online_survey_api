package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiGenerateServiceImpl implements AiGenerateService {

    private final ChatClient chatClient;
    private static final String LITE_MODEL = "gemini-2.5-flash-lite";

    public AiGenerateServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                    You are a professional survey creator. 
                    Generate survey questions based on the user's requirements.
                    Provide labels in Khmer if the prompt is in Khmer, otherwise use English.
                    Ensure the output is a valid JSON array of questions.
                    """)
                .build();
    }

    @Override
    public List<AiQuestionResponse> generateSurvey(AiGenerateRequest request) {
        try {
            return executePrompt(request, null);
        } catch (Exception e) {
            // 2. Check if the error message contains "429" (Quota/Rate Limit exceeded)
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.info("Quota exceeded. Falling back to Lite model...");

                //return executePrompt(request, LITE_MODEL);
            }
            throw e;
        }
    }

    private List<AiQuestionResponse> executePrompt(AiGenerateRequest request, String modelOverride) {
        var promptSpec = chatClient.prompt()
                .user(u -> u.text("""
                        Create a {type} survey titled '{title}'.
                        Goal: {prompt}
                        Number of questions: {count}
                        """)
                        .param("type", request.surveyType())
                        .param("title", request.surveyTitle())
                        .param("prompt", request.prompt())
                        .param("count", request.numberOfQuestions()));

        // Apply model override only if the fallback is triggered
        if (modelOverride != null) {
            promptSpec.options(GoogleGenAiChatOptions.builder()
                    .model(modelOverride)
                    .build());
        }

        return promptSpec.call()
                .entity(new ParameterizedTypeReference<List<AiQuestionResponse>>() {});
    }
}