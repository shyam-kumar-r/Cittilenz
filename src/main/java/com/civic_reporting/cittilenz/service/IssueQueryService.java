package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface IssueQueryService {

    // ========================
    // Citizen (DO NOT TOUCH)
    // ========================

    Issue getIssueById(Integer issueId);

    List<Issue> getIssuesByReporter(Integer reporterId);

    IssueResponse getIssueResponse(Integer issueId);

    // ========================
    // Legacy (kept for safety)
    // ========================

    List<Issue> getIssuesByWard(Integer wardId);

    List<Issue> getIssuesByDepartment(Integer departmentId);

    List<Issue> getIssuesByReporterAdmin(Integer reporterId);

    List<Issue> getIssuesByStatus(IssueStatus status);

    List<Issue> getIssuesForOfficial(Integer wardId, Integer departmentId);

    List<Issue> getIssuesForOfficialByStatus(
            Integer wardId,
            Integer departmentId,
            IssueStatus status
    );

    List<Issue> getIssuesByWardAndStatus(
            Integer wardId,
            IssueStatus status
    );

    // ========================
    // 🚀 NEW PRODUCTION FILTER ENGINE
    // ========================

    org.springframework.data.domain.Page<Issue> filterIssues(
            Integer wardId,
            Integer departmentId,
            Integer reportedBy,
            IssueStatus status,
            UserRole role,
            Integer userWardId,
            Integer userDepartmentId,
            org.springframework.data.domain.Pageable pageable
    );
}
