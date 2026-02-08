package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.PasswordChangeRequest;
import com.civic_reporting.cittilenz.dto.request.UserRegisterRequest;
import com.civic_reporting.cittilenz.dto.request.UserUpdateRequest;
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
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request) {

        return ResponseEntity.ok(
                UserMapper.toResponse(userService.registerCitizen(request))
        );
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserMapper.toResponse(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public UserResponse update(@RequestBody UserUpdateRequest request) {
        return UserMapper.toResponse(userService.updateCurrentUser(request));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/me/deactivate")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Void> deactivate() {
        userService.deactivateCurrentUser();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Void> delete(HttpServletRequest request) {
        userService.deleteCurrentUser(request);
        return ResponseEntity.ok().build();
    }


}
