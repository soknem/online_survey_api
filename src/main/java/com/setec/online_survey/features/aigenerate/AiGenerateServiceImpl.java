package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiGenerateServiceImpl implements AiGenerateService {

    private final ChatClient chatClient;

    // Define the Levels and their corresponding Model IDs in order
    private static final Map<String, String> MODEL_LEVELS = new LinkedHashMap<>();

    static {
        MODEL_LEVELS.put("ADVANCE", "gemini-3-flash-preview");
        MODEL_LEVELS.put("HARD", "gemini-2.5-flash");
        MODEL_LEVELS.put("MID", "gemini-2.5-flash-lite");
        MODEL_LEVELS.put("LOW", "gemma-3-27b-it");
        MODEL_LEVELS.put("VERY_LOW", "gemma-3-12b-it");
    }

    public AiGenerateServiceImpl(ChatClient.Builder builder) {
        // Build client without default system prompt to maintain Gemma compatibility
        this.chatClient = builder.build();
    }

    @Override
    public List<AiQuestionResponse> generateSurvey(AiGenerateRequest request) {
        // 1. Determine the starting point based on frontend request (default to ADVANCE)
        String requestedLevel = (request.level() != null) ? request.level().toUpperCase() : "ADVANCE";

        // 2. Create a sub-list of models starting from the requested level
        List<String> priorityChain = getPriorityChain(requestedLevel);

        Exception lastException = null;

        // 3. Loop through the chain
        for (String modelName : priorityChain) {
            try {
                log.info("Attempting survey generation with model: {}", modelName);
                return executePrompt(request, modelName);
            } catch (Exception e) {
                lastException = e;
                log.warn("Model {} failed ({}). Trying next in chain...", modelName, e.getMessage());

                // If it's a validation error (not a quota/server error), stop immediately
                if (e.getMessage() != null && e.getMessage().contains("400")) {
                    log.error("Validation error, stopping chain.");
                    break;
                }
            }
        }

        throw new RuntimeException("All models in the chain failed. Last error: " +
                (lastException != null ? lastException.getMessage() : "Unknown"), lastException);
    }

    /**
     * Reusable logic to call the AI with the merged "Universal Prompt"
     */
    private List<AiQuestionResponse> executePrompt(AiGenerateRequest request, String modelName) {
        return chatClient.prompt()
                .options(GoogleGenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(0.7)
                        .build())
                .user(u -> u.text("""
                        # SYSTEM INSTRUCTIONS
                        You are a professional survey creator. 
                        Always output valid JSON. 
                        If the prompt is in Khmer, provide labels in Khmer.
                        
                        # TASK
                        Create a {type} survey titled '{title}'.
                        Goal: {prompt}
                        Questions: {count}
                        
                        # OUTPUT FORMAT
                        Return only a JSON array of questions. Ensure Khmer text is properly formatted.
                        """)
                        .param("type", request.surveyType())
                        .param("title", request.surveyTitle())
                        .param("prompt", request.prompt())
                        .param("count", request.numberOfQuestions()))
                .call()
                .entity(new ParameterizedTypeReference<List<AiQuestionResponse>>() {});
    }

    /**
     * Returns a list of models starting from the requested level down to LOW.
     */
    private List<String> getPriorityChain(String startLevel) {
        List<String> allLevels = new ArrayList<>(MODEL_LEVELS.keySet());
        int startIndex = allLevels.indexOf(startLevel);

        // If level not found, start from the beginning (ADVANCE)
        if (startIndex == -1) startIndex = 0;

        List<String> chain = new ArrayList<>();
        for (int i = startIndex; i < allLevels.size(); i++) {
            chain.add(MODEL_LEVELS.get(allLevels.get(i)));
        }
        return chain;
    }
}