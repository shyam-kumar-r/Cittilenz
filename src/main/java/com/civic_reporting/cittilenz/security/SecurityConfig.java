package com.civic_reporting.cittilenz.security;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        http

            /*
             * =========================================
             * DISABLE CSRF
             * =========================================
             */

            .csrf(csrf -> csrf.disable())

            /*
             * =========================================
             * ENABLE CORS
             * =========================================
             */

            .cors(cors -> {})

            /*
             * =========================================
             * STATELESS SESSION
             * =========================================
             */

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            /*
             * =========================================
             * AUTHORIZATION RULES
             * =========================================
             */

            .authorizeHttpRequests(auth -> auth

                /*
                 * =========================================
                 * INFRASTRUCTURE ENDPOINTS
                 * =========================================
                 */

                .requestMatchers(
                        "/actuator/**",
                        "/error"
                ).permitAll()

                /*
                 * =========================================
                 * PUBLIC ENDPOINTS
                 * =========================================
                 */

                .requestMatchers(
                        "/auth/login",
                        "/users/register"
                ).permitAll()

                .requestMatchers(
                        "/uploads/**"
                ).permitAll()

                /*
                 * =========================================
                 * AI ENDPOINTS
                 * =========================================
                 */

                .requestMatchers(
                        "/ai/predict"
                ).authenticated()

                /*
                 * =========================================
                 * ADMIN ENDPOINTS
                 * =========================================
                 */

                .requestMatchers(
                        "/admin/**"
                ).hasRole("ADMIN")

                /*
                 * =========================================
                 * OFFICIAL ACTIONS
                 * =========================================
                 */

                .requestMatchers(
                        HttpMethod.POST,
                        "/issues/*/start"
                ).hasRole("OFFICIAL")

                .requestMatchers(
                        HttpMethod.POST,
                        "/issues/*/resolve"
                ).hasRole("OFFICIAL")

                .requestMatchers(
                        HttpMethod.POST,
                        "/issues/*/reassign"
                ).hasRole("WARD_SUPERIOR")

                /*
                 * =========================================
                 * BLOCK DIRECT MODIFICATIONS
                 * =========================================
                 */

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/issues/**"
                ).denyAll()

                .requestMatchers(
                        HttpMethod.PUT,
                        "/issues/**"
                ).denyAll()

                /*
                 * =========================================
                 * INTERNAL ENDPOINTS
                 * =========================================
                 */

                .requestMatchers(
                        "/internal/**"
                ).denyAll()

                /*
                 * =========================================
                 * EVERYTHING ELSE REQUIRES AUTH
                 * =========================================
                 */

                .anyRequest().authenticated()
            )

            /*
             * =========================================
             * EXCEPTION HANDLING
             * =========================================
             */

            .exceptionHandling(ex -> ex

                .authenticationEntryPoint((req, res, e) -> {

                    if (!res.isCommitted()) {

                        res.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );

                        res.setContentType(
                                "application/json"
                        );

                        mapper.writeValue(
                                res.getOutputStream(),
                                ApiResponse.error(
                                        "Unauthorized",
                                        401
                                )
                        );
                    }
                })

                .accessDeniedHandler((req, res, e) -> {

                    if (!res.isCommitted()) {

                        res.setStatus(
                                HttpServletResponse.SC_FORBIDDEN
                        );

                        res.setContentType(
                                "application/json"
                        );

                        mapper.writeValue(
                                res.getOutputStream(),
                                ApiResponse.error(
                                        "Forbidden",
                                        403
                                )
                        );
                    }
                })
            );

        /*
         * =========================================
         * JWT FILTER
         * =========================================
         */

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}