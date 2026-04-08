package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.*;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            NotificationService notificationService,
                            NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public UserResponse createUser(AdminUserCreateRequest req) {

        UserRole role = UserRole.valueOf(req.getRole());

        if (role == UserRole.CITIZEN) {
            throw new IllegalArgumentException("Admin cannot create citizen");
        }

        if (req.getWardId() == null) {
            throw new IllegalArgumentException("Ward is required");
        }

        if (role == UserRole.OFFICIAL && req.getDepartmentId() == null) {
            throw new IllegalArgumentException("Department required for official");
        }

        if (userRepository.existsByUsername(req.getUsername()))
            throw new IllegalStateException("Username already exists");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalStateException("Email already exists");

        if (userRepository.existsByMobile(req.getMobile()))
            throw new IllegalStateException("Mobile already exists");

        User user = new User();
        user.setUsername(req.getUsername());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setMobile(req.getMobile());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        user.setWardId(req.getWardId());
        user.setDepartmentId(
                role == UserRole.OFFICIAL ? req.getDepartmentId() : null
        );
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        notificationService.notifyUser(
                saved.getId(),
                "Account Created by Admin",
                "ADMIN_USER_CREATED",
                "ADMIN_USER_CREATED",
                null
        );

        return UserMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUser(Integer id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    @Override
    public User updateUser(Integer userId, AdminUserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());

        if (request.getEmail() != null)
            user.setEmail(request.getEmail());

        if (request.getMobile() != null)
            user.setMobile(request.getMobile());

        if (request.getIsActive() != null)
            user.setActive(request.getIsActive());

        return userRepository.save(user);
    }

    @Override
    public void toggleStatus(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Integer id, AdminPasswordResetRequest req) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.CITIZEN) {
            throw new IllegalStateException("Cannot reset citizen password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        notificationService.notifyUser(
                user.getId(),
                "Password Reset by Admin",
                "ADMIN_PASSWORD_RESET",
                "ADMIN_PASSWORD_RESET",
                null
        );
    }

    @Override
    public void deleteUser(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        userRepository.delete(user);

        Notification n = new Notification();
        n.setUserId(id);
        n.setEmail(user.getEmail());
        n.setTitle("Account Deleted by Admin");
        n.setMessage("ADMIN_USER_DELETED");
        n.setNotificationType("ADMIN_USER_DELETED");
        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setRetryCount(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setActive(true);

        notificationRepository.save(n);
    }
}