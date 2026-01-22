package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiGenerateServiceImpl implements AiGenerateService {

    private final GoogleGenAiChatModel googleChatModel;
    private final OpenAiChatModel openAiChatModel; // Standard OpenAI
    private final OpenAiChatModel groqChatModel;   // Custom Groq bean

    @Override
    public List<AiQuestionResponse> generateSurvey(AiGenerateRequest request) {
        String provider = (request.provider() != null) ? request.provider().toLowerCase() : "google";
        String requestedLevel = (request.level() != null) ? request.level().toUpperCase() : "ADVANCE";

        List<String> modelChain = getModelChain(provider, requestedLevel);
        Exception lastException = null;

        for (String modelName : modelChain) {
            try {
                log.info("Generating with {}: {}", provider, modelName);
                return executePrompt(request, provider, modelName);
            } catch (Exception e) {
                lastException = e;
                log.warn("Model {} failed: {}", modelName, e.getMessage());
                // Stop on 401/400 errors (Auth or Syntax)
                if (e.getMessage().contains("401") || e.getMessage().contains("400")) break;
            }
        }
        throw new RuntimeException("All models failed for " + provider + ": " + lastException.getMessage());
    }


    private List<AiQuestionResponse> executePrompt(AiGenerateRequest request, String provider, String modelName) {
        ChatModel activeModel = switch (provider) {
            case "groq" -> groqChatModel;
            case "openai" -> openAiChatModel;
            default -> googleChatModel;
        };

        ChatOptions options = provider.equals("google")
                ? GoogleGenAiChatOptions.builder().model(modelName).temperature(0.7).build()
                : OpenAiChatOptions.builder().model(modelName).temperature(0.7).build();

        return ChatClient.create(activeModel).prompt()
                .options(options)
                .user(u -> u.text("""
                            # ROLE
                            You are an expert Survey Designer fluent in both English and Khmer.
                                        
                            # LANGUAGE LOGIC (STRICT)
                            1. MANDATORY OVERRIDE: If the user explicitly asks for a language in the {prompt} (e.g., "Write in Khmer" or "Use English"), you MUST use that language.
                            2. AUTO-DETECTION: If no language is specified in the {prompt}, detect the language used in the {prompt} and {title}.
                               - If {prompt} is in English -> Response must be in English.
                               - If {prompt} is in Khmer -> Response must be in Khmer.
                            3. SCRIPT: For Khmer, use correct Unicode and professional "សូម" (polite) tone.
                                        
                            # SYSTEM INSTRUCTIONS
                            - Tone: Professional, encouraging, and clear.
                            - Format: Return ONLY a valid JSON array. No conversational filler, no markdown blocks like ```json.
                                        
                            # SURVEY TASK
                            - Title: {title}
                            - Type: {type}
                            - Number of Questions: {count}
                            - Goal/Context: {prompt}
                                        
                            # JSON STRUCTURE REQUIREMENT
                            Return an array of objects matching the 'AiQuestionResponse' schema.
                            """)
                        .param("title", request.surveyTitle())
                        .param("type", request.surveyType())
                        .param("prompt", request.prompt())
                        .param("count", request.numberOfQuestions()))
                .call()
                .entity(new ParameterizedTypeReference<List<AiQuestionResponse>>() {
                });
    }

    private List<String> getModelChain(String provider, String startLevel) {
        Map<String, String> levels = new LinkedHashMap<>();
        if (provider.equals("groq")) {
            levels.put("ADVANCE", "llama-3.3-70b-versatile"); // Smartest
            levels.put("HARD", "llama-3.1-8b-instant");       // Fast
            levels.put("MID", "mixtral-8x7b-32768");           // Reliable
            levels.put("LOW", "gemma2-9b-it");                 // Light
        } else if (provider.equals("openai")) {
            levels.put("ADVANCE", "gpt-4o");
            levels.put("HARD", "gpt-4o-mini");
        } else {
            levels.put("ADVANCE", "gemini-3-flash-preview");
            levels.put("HARD", "gemini-2.5-flash");
            levels.put("MID", "gemini-2.5-flash-lite");
            levels.put("LOW", "gemma-3-27b-it");
            levels.put("VERY_LOW", "gemma-3-12b-it");
        }

        List<String> keys = new ArrayList<>(levels.keySet());
        int index = Math.max(0, keys.indexOf(startLevel));
        List<String> chain = new ArrayList<>();
        for (int i = index; i < keys.size(); i++) {
            chain.add(levels.get(keys.get(i)));
        }
        return chain;
    }

    @Override
    public List<AiQuestionResponse> generateSurveyMultimodal(
            AiGenerateRequest request,
            Resource fileResource,
            String mimeType) {

        String provider = (request.provider() != null) ? request.provider().toLowerCase() : "google";
        String modelName = getModelChain(provider, request.level()).get(0);

        // Multimodal is only supported for Google (Gemini/Gemma 3) in this logic
        if (!provider.equals("google")) {
            return executePrompt(request, provider, modelName);
        }

        log.info("Generating Multimodal with Google: {} (Mime: {})", modelName, mimeType);

        // 1. Create the Media object
        var media = new Media(MimeTypeUtils.parseMimeType(mimeType), fileResource);

        // 2. Build the System Instructions (Same as your text logic)
        String instructions = """
        # ROLE
        Expert Survey Designer (English/Khmer).
        
        # TASK
        Analyze the attached input (image/audio) and the prompt: "{prompt}"
        to generate a survey with {count} questions.
        Title: {title}, Type: {type}
        
        # JSON FORMAT
        Return ONLY a JSON array matching 'AiQuestionResponse'.
        """;

        // 3. Use ChatClient with Media
        return ChatClient.create(googleChatModel).prompt()
                .options(GoogleGenAiChatOptions.builder().model(modelName).temperature(0.7).build())
                .user(u -> u
                        .text(instructions)
                        .param("prompt", request.prompt())
                        .param("title", request.surveyTitle())
                        .param("type", request.surveyType())
                        .param("count", request.numberOfQuestions())
                        .media(media) // <--- This attaches the Image or Audio
                )
                .call()
                .entity(new ParameterizedTypeReference<List<AiQuestionResponse>>() {});
    }
}