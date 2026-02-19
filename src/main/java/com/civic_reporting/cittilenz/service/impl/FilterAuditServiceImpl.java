package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.FilterAuditLog;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.FilterAuditLogRepository;
import com.civic_reporting.cittilenz.service.FilterAuditService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
public class FilterAuditServiceImpl implements FilterAuditService {

    private final FilterAuditLogRepository repository;

    public FilterAuditServiceImpl(FilterAuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    @Override
    public void logFilterUsage(
            String username,
            UserRole role,
            Integer wardId,
            Integer departmentId,
            Integer reportedBy,
            String status,
            Pageable pageable,
            long resultCount
    ) {

        try {

            FilterAuditLog log = new FilterAuditLog();

            log.setUsername(username);
            log.setRole(role.name());
            log.setWardId(wardId);
            log.setDepartmentId(departmentId);
            log.setReportedBy(reportedBy);
            log.setStatus(status);
            log.setPageNumber(pageable.getPageNumber());
            log.setPageSize(pageable.getPageSize());
            log.setResultCount(resultCount);

            repository.save(log);

        } catch (Exception ignored) {
            // Never break main flow
        }
    }
}
