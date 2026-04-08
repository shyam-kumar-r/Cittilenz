package com.civic_reporting.cittilenz.client;

import com.civic_reporting.cittilenz.dto.response.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AiModelClient {

    private final WebClient webClient;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public AiModelClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public AiResponse predict(MultipartFile file) {

        try {

            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            AiResponse response = webClient.post()
                    .uri(aiServiceUrl + "/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response == null) {
                throw new IllegalStateException("AI service returned empty response");
            }

            return response;

        } catch (Exception e) {
            throw new IllegalStateException("AI service unavailable");
        }
    }
}