package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.FilterAuditLog;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.FilterAuditLogRepository;
import com.civic_reporting.cittilenz.service.FilterAuditService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
public class FilterAuditServiceImpl implements FilterAuditService {

    private static final Logger log =
            LoggerFactory.getLogger(FilterAuditServiceImpl.class);

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

            FilterAuditLog logEntity = new FilterAuditLog();

            logEntity.setUsername(username);
            logEntity.setRole(role.name());
            logEntity.setWardId(wardId);
            logEntity.setDepartmentId(departmentId);
            logEntity.setReportedBy(reportedBy);
            logEntity.setStatus(status);
            logEntity.setPageNumber(pageable.getPageNumber());
            logEntity.setPageSize(pageable.getPageSize());
            logEntity.setResultCount(resultCount);

            repository.save(logEntity);

        } catch (Exception ex) {
            log.warn("Filter audit logging failed", ex);
        }
    }
}