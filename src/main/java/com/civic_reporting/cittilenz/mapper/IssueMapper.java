package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.IssueHistoryResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.IssueHistory;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.repository.IssueHistoryRepository;
import com.civic_reporting.cittilenz.repository.IssueTypeRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.AssignmentService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IssueMapper {

    private final IssueHistoryRepository issueHistoryRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final UserRepository userRepository;
    private final AssignmentService assignmentService;

    public IssueMapper(
            IssueHistoryRepository issueHistoryRepository,
            IssueTypeRepository issueTypeRepository,
            UserRepository userRepository,
            AssignmentService assignmentService
    ) {
        this.issueHistoryRepository = issueHistoryRepository;
        this.issueTypeRepository = issueTypeRepository;
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
    }

    public IssueResponse toResponse(Issue issue) {

        if (issue == null) {
            return null;
        }

        IssueResponse response = new IssueResponse();

        // =========================
        // Core
        // =========================
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setImageUrl(issue.getImageUrl());
        response.setLatitude(issue.getLatitude());
        response.setLongitude(issue.getLongitude());

        // =========================
        // Issue Type Name (Safe)
        // =========================
        if (issue.getIssueTypeId() != null) {
            issueTypeRepository.findById(issue.getIssueTypeId())
                    .ifPresent(type -> response.setIssueTypeName(type.getName()));
        }

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
        // Official (Safe + Active Check)
        // =========================
        if (issue.getAssignedOfficialId() != null) {

            Optional<User> official =
                    userRepository.findByIdAndActiveTrue(issue.getAssignedOfficialId());

            official.ifPresent(user -> {
                response.setAssignedOfficialName(user.getFullName());
                response.setAssignedOfficialMobile(user.getMobile());
                response.setAssignedOfficialEmail(user.getEmail());
            });
        }

        // =========================
        // Ward Superior (Production Safe)
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
        response.setResolvedAt(issue.getResolvedAt());
        response.setSlaDeadline(issue.getSlaDeadline());
        response.setActive(issue.isActive());

        // =========================
        // History (Always Ordered)
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

        if (history.getChangedBy() != null) {
            userRepository.findByIdAndActiveTrue(history.getChangedBy())
                    .ifPresent(user -> dto.setChangedByName(user.getFullName()));
        }

        return dto;
    }
}
