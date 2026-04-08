package com.civic_reporting.cittilenz.security;

import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.repository.UserRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid credentials"));

        if (!user.isActive()) {
            throw new DisabledException("Account deactivated");
        }

        return new UserPrincipal(user);
    }
}