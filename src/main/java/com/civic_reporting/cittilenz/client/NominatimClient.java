package com.civic_reporting.cittilenz.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
public class NominatimClient {

    private static final Logger log =
            LoggerFactory.getLogger(NominatimClient.class);

    private final WebClient webClient;

    public NominatimClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map reverseGeocode(Double latitude, Double longitude) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("/reverse")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("format", "json")
                        .queryParam("addressdetails", 1)
                        .build())
                .header("User-Agent", "cittilenz-backend/1.0 (shyamkumar71521@gmail.com)")
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(
                        Retry.fixedDelay(1, Duration.ofSeconds(1))
                                .filter(ex -> ex instanceof IOException)
                )
                .block();
    }
}
