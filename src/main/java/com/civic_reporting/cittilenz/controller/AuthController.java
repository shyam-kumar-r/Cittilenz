package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.LoginRequest;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.security.JwtUtil;
import com.civic_reporting.cittilenz.security.UserPrincipal;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getIdentifier(),
                                    request.getPassword()
                            )
                    );

            UserPrincipal principal =
                    (UserPrincipal) authentication.getPrincipal();

            if (!principal.getUser().isActive()) {
                throw new IllegalStateException("Account deactivated");
            }

            String token = jwtUtil.generateToken(principal.getUsername());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Login successful",
                            Map.of(
                                    "token", token,
                                    "user", UserMapper.toResponse(principal.getUser())
                            )
                    )
            );

        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    // =========================
    // CURRENT USER
    // =========================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthorized");
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Unauthorized");
        }

        if (!principal.getUser().isActive()) {
            throw new IllegalStateException("Account deactivated");
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        UserMapper.toResponse(principal.getUser())
                )
        );
    }

    // =========================
    // LOGOUT
    // =========================
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful", null)
        );
    }
}