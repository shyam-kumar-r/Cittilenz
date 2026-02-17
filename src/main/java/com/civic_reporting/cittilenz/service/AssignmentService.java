package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.User;

import java.util.Optional;

public interface AssignmentService {

    Optional<User> assignOfficial(Integer wardId, Integer departmentId);

    Optional<User> getWardSuperior(Integer wardId);
}
