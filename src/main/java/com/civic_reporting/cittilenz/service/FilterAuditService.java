package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.enums.UserRole;
import org.springframework.data.domain.Pageable;

public interface FilterAuditService {

    void logFilterUsage(
            String username,
            UserRole role,
            Integer wardId,
            Integer departmentId,
            Integer reportedBy,
            String status,
            Pageable pageable,
            long resultCount
    );
}
