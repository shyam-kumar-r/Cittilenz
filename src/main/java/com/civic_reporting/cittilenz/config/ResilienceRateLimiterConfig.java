package com.civic_reporting.cittilenz.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceRateLimiterConfig {

    @Bean
    public RateLimiter nominatimRateLimiter() {

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1)) // 1 second window
                .limitForPeriod(1)                         // 1 request per window
                .timeoutDuration(Duration.ofSeconds(2))    // wait max 2 sec
                .build();

        return RateLimiter.of("nominatimRateLimiter", config);
    }
}
