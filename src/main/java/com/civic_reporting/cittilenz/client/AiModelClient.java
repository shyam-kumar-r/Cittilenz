package com.civic_reporting.cittilenz.client;

import com.civic_reporting.cittilenz.dto.response.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

@Component
public class AiModelClient {

    private final WebClient webClient;

    @Value("${ai.service.base-url}")
    private String aiServiceUrl;

    public AiModelClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public AiResponse predict(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

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
                    .timeout(Duration.ofSeconds(95)) // extra safety beyond WebClient timeout
                    .retryWhen(
                            Retry.backoff(2, Duration.ofSeconds(5))
                                 .maxBackoff(Duration.ofSeconds(15))
                    )
                    .block();

            if (response == null || response.getIssue() == null) {
                throw new IllegalStateException("Invalid response from AI service");
            }

            return response;

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file for AI processing", e);
        } catch (Exception e) {
            throw new IllegalStateException("AI service unavailable: " + e.getMessage(), e);
        }
    }
}