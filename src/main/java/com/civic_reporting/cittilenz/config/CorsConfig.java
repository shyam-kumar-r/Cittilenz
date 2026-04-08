package com.civic_reporting.cittilenz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class CorsConfig {

	@Value("${app.cors.allowed-origins:http://localhost:3000}")
	private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 🔥 Allowed Origins (frontend URLs)
        config.setAllowedOrigins(allowedOrigins);

        // 🔥 Allowed HTTP Methods
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        // 🔥 Allowed Headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin"
        ));

        // 🔥 Exposed Headers (important for frontend)
        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        // 🔥 Credentials (JWT header usage)
        config.setAllowCredentials(true);

        // 🔥 Cache preflight response (performance)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}