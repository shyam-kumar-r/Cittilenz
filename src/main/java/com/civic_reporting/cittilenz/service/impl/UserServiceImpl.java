package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.PasswordChangeRequest;
import com.civic_reporting.cittilenz.dto.request.UserRegisterRequest;
import com.civic_reporting.cittilenz.dto.request.UserUpdateRequest;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.security.UserPrincipal;
import com.civic_reporting.cittilenz.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================================================
       Helper: get authenticated principal
       ========================================================= */
    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
    
    private void ensureCitizenOnly(User user) {
        if (user.getRole() != UserRole.CITIZEN) {
            throw new AccessDeniedException(
                "Only citizens can perform this action"
            );
        }
    }

    /* =========================================================
       Registration (Citizen only)
       ========================================================= */
    @Override
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

        return userRepository.save(user);
    }

    /* =========================================================
       Current user
       ========================================================= */
    @Override
    public User getCurrentUser() {
        return getPrincipal().getUser();
    }

    /* =========================================================
       Update profile (partial)
       ========================================================= */
    @Override
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
       Change password
       ========================================================= */
    @Override
    public void changePassword(PasswordChangeRequest request) {

        if (request.getOldPassword() == null ||
            request.getNewPassword() == null ||
            request.getConfirmNewPassword() == null) {
            throw new IllegalArgumentException("All password fields are required");
        }

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
    }

    /* =========================================================
       Deactivate account (soft delete)
       ========================================================= */
    @Override
    public void deactivateCurrentUser() {

        User user = getCurrentUser();
        ensureCitizenOnly(user);
        user.setActive(false);
        userRepository.save(user);

        // Kill authentication immediately
        SecurityContextHolder.clearContext();
    }

    /* =========================================================
       Delete account (hard delete)
       ========================================================= */
    @Override
    public void deleteCurrentUser(HttpServletRequest request) {

        User user = getCurrentUser();
        ensureCitizenOnly(user);

        // Delete DB row
        userRepository.delete(user);

        // Invalidate HTTP session (CRITICAL)
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        // Clear SecurityContext
        SecurityContextHolder.clearContext();
    }
}
