package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.PasswordChangeRequest;
import com.civic_reporting.cittilenz.dto.request.UserRegisterRequest;
import com.civic_reporting.cittilenz.dto.request.UserUpdateRequest;
import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.security.UserPrincipal;
import com.civic_reporting.cittilenz.service.UserService;
import com.civic_reporting.cittilenz.service.NotificationService;
import com.civic_reporting.cittilenz.service.TemplateService;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final NotificationRepository notificationRepository;

    private static final String LOGO_URL =
            "https://raw.githubusercontent.com/shyam-kumar-r/Cittilenz/master/src/main/resources/static/logo.jpeg";
    
    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           NotificationService notificationService,
                           TemplateService templateService,
                           NotificationRepository notificationRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.templateService = templateService;
        this.notificationRepository = notificationRepository;
    }

    /* ========================================================= */
    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void ensureCitizenOnly(User user) {
        if (user.getRole() != UserRole.CITIZEN) {
            throw new AccessDeniedException("Only citizens allowed");
        }
    }

    /* =========================================================
       REGISTRATION
       ========================================================= */
    @Override
    @Transactional
    public User registerCitizen(UserRegisterRequest request) {

        log.info("User registration started | username={}", request.getUsername());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CITIZEN);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        notificationService.notifyUser(
                saved.getId(),
                "Welcome to Cittilenz",
                "USER_REGISTERED",
                "USER_REGISTERED",
                null
        );

        log.info("User registered successfully | userId={}", saved.getId());

        return saved;
    }


    /* ========================================================= */
    @Override
    @Transactional
    public User getCurrentUser() {
        return getPrincipal().getUser();
    }

    /* ========================================================= */
    @Override
    @Transactional
    public User updateCurrentUser(UserUpdateRequest request) {

        User user = getCurrentUser();
        ensureCitizenOnly(user);
        
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getMobile() != null &&
                !request.getMobile().equals(user.getMobile())) {

            if (userRepository.existsByMobile(request.getMobile())) {
                throw new IllegalArgumentException("Mobile already exists");
            }
            user.setMobile(request.getMobile());
        }

        return userRepository.save(user);
    }

    /* =========================================================
       PASSWORD CHANGE
       ========================================================= */
    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {

        User user = getCurrentUser();
        ensureCitizenOnly(user);

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password incorrect");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        // ✅ HTML EMAIL
        notificationService.notifyUser(
                user.getId(),
                "Password Updated",
                "PASSWORD_CHANGED",
                "PASSWORD_CHANGED",
                null
        );
    }

    /* =========================================================
       DEACTIVATE
       ========================================================= */
    @Override
    @Transactional
    public void deactivateCurrentUser() {

        User user = getCurrentUser();
        ensureCitizenOnly(user);

        user.setActive(false);
        userRepository.save(user);

        // ✅ HTML EMAIL
        notificationService.notifyUser(
                user.getId(),
                "Account Deactivated",
                "ACCOUNT_DEACTIVATED",
                "ACCOUNT_DEACTIVATED",
                null
        );

        SecurityContextHolder.clearContext();
    }

    /* =========================================================
       DELETE
       ========================================================= */
    @Override
    @PreAuthorize("hasRole('CITIZEN')")
    @Transactional
    public void deleteCurrentUser(HttpServletRequest request) {

        User user = getCurrentUser();
        ensureCitizenOnly(user);

        Integer userId = user.getId();
        String email = user.getEmail();
        String name = user.getFullName();

        log.info("Citizen deletion started | userId={}", userId);

        try {
            // ✅ HARD DELETE (Triggers will handle everything)
            userRepository.delete(user);

            log.info("Citizen deleted successfully | userId={}", userId);

        } catch (Exception ex) {
            log.error("Citizen deletion failed | userId={}", userId, ex);
            throw ex;
        }

        // ✅ ALWAYS send notification (separate txn)
        sendDeletionNotification(userId, email, name);

        // ✅ Clear session
        SecurityContextHolder.clearContext();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendDeletionNotification(Integer userId, String email, String name) {

        Notification n = new Notification();

        n.setUserId(userId);
        n.setEmail(email);
        n.setTitle("Account Deleted");
        n.setMessage("ACCOUNT_DELETED");
        n.setNotificationType("ACCOUNT_DELETED");
        n.setIssueId(null);
        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setRetryCount(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setActive(true);

        notificationRepository.save(n);

        log.info("Deletion notification queued | userId={}", userId);
    }

    /* ========================================================= */
    @Override
    @Transactional
    public User getAuthenticatedUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Unauthorized access");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}