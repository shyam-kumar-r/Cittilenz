package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.LoginRequest;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.security.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest) {

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getIdentifier(),
                                    request.getPassword()
                            )
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                    "SPRING_SECURITY_CONTEXT",
                    context
            );

            UserPrincipal principal =
                    (UserPrincipal) authentication.getPrincipal();

            if (!principal.getUser().isActive()) {
                SecurityContextHolder.clearContext();
                throw new AccessDeniedException("Account deactivated");
            }

            return UserMapper.toResponse(principal.getUser());

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );

        }

    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        if (!principal.getUser().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Account deactivated"
            );
        }

        return UserMapper.toResponse(principal.getUser());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

    }
}