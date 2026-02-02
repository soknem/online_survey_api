package com.setec.online_survey.features.aigenerate;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// Import Flux from Project Reactor
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-generate")
@RequiredArgsConstructor
public class AiGenerateController {

    @Value("${spring.ai.google.genai.api-key}")
    String API_KEY;

    private final AiGenerateService aiGenerateService;
    private final OpenAiChatModel groqChatModel;
    private final ChatClient chatClient;

    @Qualifier("groqChatClient")
    private final ChatClient groqChatClient;

    @PostMapping("/survey")
    public List<AiQuestionResponse> generate(@RequestBody AiGenerateRequest request) {
        return aiGenerateService.generateSurvey(request);
    }

    @PostMapping(value = "/smart-suggest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> smartSuggest(@RequestBody SuggestRequest request) {
        String input = request.prompt();
        if (input == null || input.isBlank()) return Flux.empty();

        return groqChatClient.prompt()
                .system("""
                You are a survey text completion engine. 
                Your ONLY task is to provide the next 2-4 sentences that follow the user's text.
                - Start exactly where the user left off.
                - Do not repeat the user's input.
                - Do not explain yourself.
                - Do not use any labels or prefixes.
                - If the input is a question like 'generate survey about...', finish it and complete the last word(ex:abou to about) and write the first 2 questions.
                """)
                .user(input) // We pass the raw text directly
                .options(OpenAiChatOptions.builder()
                        .model("llama-3.1-8b-instant")
                        .temperature(0.1) // Lower temperature for more logical flow
                        .maxTokens(150)   // Enough for 2-4 sentences
                        .stop(List.of("Input:", "Note:", "\n\n\n"))
                        .build())
                .stream()
                .content();
    }

    @GetMapping
    public GenerateContentResponse getApiDocument( ) {
        Client client = Client.builder().apiKey(API_KEY).build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        "Explain how AI works in a few words",
                        null);

//        System.out.println(response.text());
        return response;
    }

    @PostMapping(value = "/generate-multimodal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<AiQuestionResponse> createFromFile(
            @RequestPart("request") AiGenerateRequest request,
            @RequestPart("file") MultipartFile file) {

        return aiGenerateService.generateSurveyMultimodal(
                request,
                file.getResource(),
                file.getContentType()
        );
    }
}