package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.*;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User registered successfully",
                        UserMapper.toResponse(userService.registerCitizen(request))
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        UserMapper.toResponse(userService.getCurrentUser())
                )
        );
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated",
                        UserMapper.toResponse(userService.updateCurrentUser(request))
                )
        );
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null)
        );
    }

    @PatchMapping("/me/deactivate")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Void>> deactivate() {

        userService.deactivateCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success("Account deactivated", null)
        );
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            HttpServletRequest request) {

        userService.deleteCurrentUser(request);

        return ResponseEntity.ok(
                ApiResponse.success("Account deleted", null)
        );
    }
}