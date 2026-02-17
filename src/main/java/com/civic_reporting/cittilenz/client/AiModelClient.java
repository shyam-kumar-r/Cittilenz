package com.civic_reporting.cittilenz.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class AiModelClient {

    private final WebClient webClient;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public AiModelClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls Python AI microservice to classify issue image.
     *
     * Expected AI Response:
     * {
     *   "issueTypeId": 3
     * }
     */
    public Integer detectIssueType(MultipartFile image) {

        // ==========================
        // FUTURE PRODUCTION AI CALL
        // ==========================

        /*
        Map response = webClient.post()
                .uri(aiServiceUrl + "/detect")
                .bodyValue(image.getBytes()) // adjust when API finalized
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (Integer) response.get("issueTypeId");
        */

        // ======================================
        // MANUAL FALLBACK STARTS HERE
        // ======================================

        // Temporary hardcoded issueTypeId for testing
        // Change this value depending on test case
        return 1;

        // ======================================
        // MANUAL FALLBACK ENDS HERE
        // ======================================
    }
}
