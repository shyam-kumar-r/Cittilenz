package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.IssueHistoryResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.IssueHistory;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.repository.IssueHistoryRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.AssignmentService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IssueMapper {

    private final IssueHistoryRepository issueHistoryRepository;
    private final AssignmentService assignmentService;

    // 🔥 PRODUCTION SAFE BASE URL
    @Value("${app.base-url}")
    private String baseUrl;

    public IssueMapper(
            IssueHistoryRepository issueHistoryRepository,
            AssignmentService assignmentService
    ) {
        this.issueHistoryRepository = issueHistoryRepository;
        this.assignmentService = assignmentService;
    }

    public IssueResponse toResponse(Issue issue) {

        if (issue == null) return null;

        IssueResponse response = new IssueResponse();

        // =========================
        // Core
        // =========================
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());

        // 🔥 SAFE IMAGE URL
        response.setImageUrl(
                issue.getImageUrl() != null ? baseUrl + issue.getImageUrl() : null
        );

        response.setResolvedImageUrl(
                issue.getResolvedImageUrl() != null
                        ? baseUrl + issue.getResolvedImageUrl()
                        : null
        );

        response.setLatitude(issue.getLatitude());
        response.setLongitude(issue.getLongitude());

        // 🔥 USE STORED VALUE (NO DB CALL)
        response.setIssueTypeName(issue.getIssueTypeName());

        // =========================
        // Address
        // =========================
        response.setStreet(issue.getStreet());
        response.setArea(issue.getArea());
        response.setLocality(issue.getLocality());
        response.setCity(issue.getCity());
        response.setPincode(issue.getPincode());

        // =========================
        // Ward
        // =========================
        response.setWardId(issue.getWardId());
        response.setWardName(issue.getWardName());

        // =========================
        // Department
        // =========================
        response.setDepartmentId(issue.getDepartmentId());
        response.setDepartmentName(issue.getDepartmentName());

        // =========================
        // Reporter
        // =========================
        response.setReportedByName(issue.getReportedByName());

        // =========================
        // Assigned Official
        // =========================
        response.setAssignedOfficialId(issue.getAssignedOfficialId());
        response.setAssignedOfficialName(issue.getAssignedOfficialName());
        response.setAssignedOfficialEmail(issue.getAssignedOfficialEmail());

        // =========================
        // Ward Superior
        // =========================
        if (issue.getWardId() != null) {

            assignmentService.getWardSuperior(issue.getWardId())
                    .filter(User::isActive)
                    .ifPresent(superior -> {
                        response.setWardSuperiorName(superior.getFullName());
                        response.setWardSuperiorMobile(superior.getMobile());
                        response.setWardSuperiorEmail(superior.getEmail());
                    });
        }

        // =========================
        // Lifecycle
        // =========================
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setReportCount(issue.getReportCount());

        response.setCreatedAt(issue.getCreatedAt());
        response.setAssignedAt(issue.getAssignedAt());
        response.setStartedAt(issue.getStartedAt());
        response.setResolvedAt(issue.getResolvedAt());
        response.setEscalatedAt(issue.getEscalatedAt());
        response.setReassignedAt(issue.getReassignedAt());

        response.setActive(issue.getActive());

        // =========================
        // SLA
        // =========================
        response.setSoftSlaDeadline(issue.getSoftSlaDeadline());
        response.setHardSlaDeadline(issue.getHardSlaDeadline());

        response.setSoftSlaBreached(issue.getSoftSlaBreached());
        response.setHardSlaBreached(issue.getHardSlaBreached());

        response.setEscalationCount(issue.getEscalationCount());
        response.setReassignmentCount(issue.getReassignmentCount());

        response.setRequiresSupervisorIntervention(
                issue.isRequiresSupervisorIntervention()
        );

        // =========================
        // Version
        // =========================
        response.setVersion(issue.getVersion());

        // =========================
        // History
        // =========================
        List<IssueHistoryResponse> historyList =
                issueHistoryRepository
                        .findByIssueIdOrderByChangedAtAsc(issue.getId())
                        .stream()
                        .map(this::mapHistory)
                        .collect(Collectors.toList());

        response.setHistory(historyList);

        return response;
    }

    private IssueHistoryResponse mapHistory(IssueHistory history) {

        IssueHistoryResponse dto = new IssueHistoryResponse();

        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedAt(history.getChangedAt());
        dto.setRemarks(history.getRemarks());

        dto.setChangedByName(history.getChangedByName());

        return dto;
    }
}