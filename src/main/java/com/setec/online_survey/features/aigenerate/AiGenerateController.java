package com.setec.online_survey.features.aigenerate;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.setec.online_survey.features.aigenerate.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-generate")
@RequiredArgsConstructor
public class AiGenerateController {

    @Value("${spring.ai.google.genai.api-key}")
    String API_KEY;

    private final AiGenerateService aiGenerateService;

    @PostMapping("/survey")
    public List<AiQuestionResponse> generate(@RequestBody AiGenerateRequest request) {
        return aiGenerateService.generateSurvey(request);
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