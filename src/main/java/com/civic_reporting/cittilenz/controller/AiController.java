package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.client.AiModelClient;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.AiResponse;
import com.civic_reporting.cittilenz.service.AiMappingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiModelClient aiModelClient;
    private final AiMappingService aiMappingService;

    public AiController(AiModelClient aiModelClient,
                        AiMappingService aiMappingService) {
        this.aiModelClient = aiModelClient;
        this.aiMappingService = aiMappingService;
    }

    @PostMapping(value = "/predict", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AiResponse>> predictIssue(
            @RequestParam("file") MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        AiResponse aiResponse = aiModelClient.predict(file);

        if (aiResponse == null) {
            throw new IllegalStateException("AI service returned empty response");
        }

        Integer issueTypeId = aiMappingService
                .mapToIssueTypeId(aiResponse.getIssue());

        aiResponse.setIssueTypeId(issueTypeId);

        double confidence = aiResponse.getConfidence() != null
                ? aiResponse.getConfidence()
                : 0.0;

        boolean autoSelected = (confidence >= 0.7 && issueTypeId != null);

        aiResponse.setAutoSelected(autoSelected);

        return ResponseEntity.ok(
                ApiResponse.success("AI prediction successful", aiResponse)
        );
    }
}