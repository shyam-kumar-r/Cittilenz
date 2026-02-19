package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.FilterAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterAuditLogRepository
        extends JpaRepository<FilterAuditLog, Long> {
}
