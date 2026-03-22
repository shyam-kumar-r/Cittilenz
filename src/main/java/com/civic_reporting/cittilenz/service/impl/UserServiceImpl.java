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
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔥 NEW DEPENDENCIES
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final NotificationRepository notificationRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           NotificationService notificationService,
                           TemplateService templateService, NotificationRepository notificationRepository) {

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
            throw new AccessDeniedException("Only citizens can perform this action");
        }
    }

    /* =========================================================
       REGISTRATION
       ========================================================= */
    @Override
    @Transactional
    public User registerCitizen(UserRegisterRequest request) {

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

        // ✅ SAVE FIRST (CRITICAL)
        User saved = userRepository.save(user);

        // ✅ BUILD HTML TEMPLATE
        String html = templateService.build(
        	    "registration-email",
        	    Map.of(
        	        "name", saved.getFullName(),
        	        "username", saved.getUsername(),
        	        "email", saved.getEmail(),
        	        "logoUrl", "https://yourcdn.com/logo.png",
        	        "actionUrl", "https://yourapp.com/dashboard"
        	    )
        	);

        // ✅ SEND NOTIFICATION
        notificationService.notifyUser(
                saved.getId(),
                "Welcome to Cittilenz",
                html,
                "USER_REGISTERED"
        );

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

        notificationService.notifyUser(
                user.getId(),
                "Password Updated",
                "Your password has been updated successfully.",
                "PASSWORD_CHANGED"
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

        notificationService.notifyUser(
                user.getId(),
                "Account Deactivated",
                "Your account has been deactivated.",
                "ACCOUNT_DEACTIVATED"
        );

        SecurityContextHolder.clearContext();
    }

    /* =========================================================
       DELETE
       ========================================================= */
    @Override
    @PreAuthorize("hasRole('CITIZEN')")
    public void deleteCurrentUser(HttpServletRequest request) {

        System.out.println("DEBUG: deleteCurrentUser called");

        User user = getCurrentUser();

        ensureCitizenOnly(user);

        // ✅ STORE BEFORE DELETE
        Integer userId = user.getId();
        String email = user.getEmail();

        // ✅ DELETE USER
        userRepository.delete(user);

        System.out.println("DEBUG: user deleted");

        // ✅ SEND NOTIFICATION (SEPARATE TX)
        sendDeletionNotification(userId, email);

        SecurityContextHolder.clearContext();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendDeletionNotification(Integer userId, String email) {

        Notification n = new Notification();

        n.setUserId(userId);  // no FK now → safe
        n.setEmail(email);    // critical
        n.setTitle("Account Deleted");
        n.setMessage("Your account has been permanently deleted.");
        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setRetryCount(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setActive(true);

        notificationRepository.save(n);
    }

    /* =========================================================
       TEMPLATE LOADER (SIMPLE VERSION)
       ========================================================= */
    private String loadRegistrationTemplate() {
        return """
            <html>
            <body>
            <h2>Welcome to Cittilenz</h2>
            <p>Hello <b>{{name}}</b>,</p>
            <p>Your account has been successfully created.</p>
            <p><b>Username:</b> {{username}}</p>
            <p><b>Email:</b> {{email}}</p>
            </body>
            </html>
            """;
    }
    
    @Override
    @Transactional
    public User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}