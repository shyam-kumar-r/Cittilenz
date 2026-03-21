package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.SystemUserService;
import org.springframework.stereotype.Service;

@Service
public class SystemUserServiceImpl implements SystemUserService {

    private final UserRepository userRepository;

    public SystemUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getSystemUser() {
        return userRepository.findByRole(UserRole.SYSTEM)
                .orElseThrow(() ->
                        new IllegalStateException("SYSTEM user not configured in database"));
    }
}
