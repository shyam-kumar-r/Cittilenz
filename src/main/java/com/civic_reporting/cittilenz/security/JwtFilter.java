package com.civic_reporting.cittilenz.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        /*
         * =========================================
         * SKIP INFRASTRUCTURE ENDPOINTS
         * =========================================
         */

        if (
                path.startsWith("/actuator") ||
                path.startsWith("/error")
        ) {

            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader =
                request.getHeader("Authorization");

        String username = null;
        String token = null;

        /*
         * =========================================
         * EXTRACT JWT TOKEN
         * =========================================
         */

        if (
                authHeader != null &&
                authHeader.startsWith("Bearer ")
        ) {

            token = authHeader.substring(7);

            try {

                username = jwtUtil.extractUsername(token);

            } catch (Exception ex) {

                log.warn(
                        "Invalid JWT token for request path: {}",
                        path
                );
            }
        }

        /*
         * =========================================
         * AUTHENTICATE USER
         * =========================================
         */

        if (
                username != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null
        ) {

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);

            if (
                    jwtUtil.validateToken(
                            token,
                            userDetails.getUsername()
                    )
            ) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}