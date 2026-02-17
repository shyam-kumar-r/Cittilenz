package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.AssignmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRepository userRepository;

    public AssignmentServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> assignOfficial(Integer wardId, Integer departmentId) {

        List<User> officials =
                userRepository.findByRoleAndWardIdAndDepartmentIdAndActiveTrue(
                        UserRole.OFFICIAL,
                        wardId,
                        departmentId
                );

        if (officials.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(officials.get(0));
    }

    @Override
    public Optional<User> getWardSuperior(Integer wardId) {

        List<User> superiors =
                userRepository.findByRoleAndWardIdAndActiveTrue(
                        UserRole.WARD_SUPERIOR,
                        wardId
                );

        if (superiors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(superiors.get(0));
    }
}
