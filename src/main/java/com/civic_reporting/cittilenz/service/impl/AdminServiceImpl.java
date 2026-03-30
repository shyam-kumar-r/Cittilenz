package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.AdminPasswordResetRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserCreateRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.UserMapper;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.AdminService;
import com.civic_reporting.cittilenz.service.NotificationService;
import com.civic_reporting.cittilenz.service.TemplateService;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final NotificationRepository notificationRepository;

    private static final String LOGO_URL =
    "https://raw.githubusercontent.com/shyam-kumar-r/Cittilenz/master/src/main/resources/static/logo.jpeg";

    public AdminServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService,
            TemplateService templateService, NotificationRepository notificationRepository) {
    	this.userRepository = userRepository;
    	this.passwordEncoder = passwordEncoder;
    	this.notificationService = notificationService;
    	this.templateService = templateService;
    	this.notificationRepository = notificationRepository;
    	}

    @Override
    public UserResponse createUser(AdminUserCreateRequest req) {

        UserRole role = UserRole.valueOf(req.getRole());

        if (role == UserRole.CITIZEN) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Admin cannot create citizen");
        }

        if (req.getWardId() == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Ward is required");
        }

        if (role == UserRole.OFFICIAL && req.getDepartmentId() == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Department required for official");
        }

        if (userRepository.existsByUsername(req.getUsername()))
            throw new ResponseStatusException(CONFLICT, "Username exists");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new ResponseStatusException(CONFLICT, "Email exists");

        if (userRepository.existsByMobile(req.getMobile()))
            throw new ResponseStatusException(CONFLICT, "Mobile exists");

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

     // ✅ BUILD EMAIL
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
                        new ResponseStatusException(NOT_FOUND));
    }
    
    @Override
    @Transactional
    public User updateUser(Integer userId, AdminUserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserRole role = user.getRole();

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());

        if (request.getEmail() != null)
            user.setEmail(request.getEmail());

        if (request.getMobile() != null)
            user.setMobile(request.getMobile());

        if (request.getIsActive() != null)
            user.setActive(request.getIsActive());

        // ROLE-SPECIFIC RULES
        switch (role) {

            case CITIZEN -> {
                // ward & department must stay NULL
                user.setWardId(null);
                user.setDepartmentId(null);
            }

            case OFFICIAL -> {
                if (request.getWardId() != null)
                    user.setWardId(request.getWardId());

                if (request.getDepartmentId() != null)
                    user.setDepartmentId(request.getDepartmentId());
            }

            case WARD_SUPERIOR -> {
                if (request.getWardId() != null)
                    user.setWardId(request.getWardId());

                user.setDepartmentId(null);
            }

            case ADMIN -> {
                user.setWardId(null);
                user.setDepartmentId(null);
            }
        }

        return userRepository.save(user);
    }


    @Override
    public void toggleStatus(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(NOT_FOUND));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Integer id, AdminPasswordResetRequest req) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(NOT_FOUND));

        if (user.getRole() == UserRole.CITIZEN) {
            throw new ResponseStatusException(
                    FORBIDDEN, "Admin cannot reset citizen password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // ✅ EMAIL
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
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        String name = user.getFullName();
        String email = user.getEmail();

        // ✅ DELETE USER
        userRepository.delete(user);

        // ✅ DIRECT NOTIFICATION SAVE (NO USER LOOKUP)
        Notification n = new Notification();

        n.setUserId(id);
        n.setEmail(email);
        n.setTitle("Account Deleted by Admin");
        n.setMessage("ADMIN_USER_DELETED");
        n.setNotificationType("ADMIN_USER_DELETED");
        n.setIssueId(null); // ✅ FIX

        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setRetryCount(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setActive(true);

        notificationRepository.save(n);
    }
}
