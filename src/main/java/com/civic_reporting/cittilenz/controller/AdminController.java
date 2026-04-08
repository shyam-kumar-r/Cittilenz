package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.AdminPasswordResetRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserCreateRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody AdminUserCreateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User created successfully",
                        adminService.createUser(request)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {

        return ResponseEntity.ok(
                ApiResponse.success(adminService.getAllUsers())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                ApiResponse.success(adminService.getUser(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserByAdmin(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        User user = adminService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        UserMapper.toResponse(user)
                )
        );
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody AdminPasswordResetRequest request) {

        adminService.resetPassword(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successful", null)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id) {

        adminService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }
}