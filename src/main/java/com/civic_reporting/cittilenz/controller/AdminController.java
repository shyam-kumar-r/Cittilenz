package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.AdminPasswordResetRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserCreateRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserUpdateRequest;
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
    public UserResponse create(@Valid @RequestBody AdminUserCreateRequest request) {
        return adminService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> list() {
        return adminService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Integer id) {
        return adminService.getUser(id);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserByAdmin(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        User user = adminService.updateUser(id, request);
        return UserMapper.toResponse(user);
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Integer id) {
        adminService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody AdminPasswordResetRequest request) {

        adminService.resetPassword(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
