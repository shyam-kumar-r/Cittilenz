package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.response.IssueHistoryResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.repository.IssueHistoryRepository;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.IssueQueryService;
import com.civic_reporting.cittilenz.specification.IssueSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Service;
import com.civic_reporting.cittilenz.mapper.IssueMapper;

import java.util.List;

@Service
public class IssueQueryServiceImpl implements IssueQueryService {
	
	private static final Logger log =
	        LoggerFactory.getLogger(IssueQueryServiceImpl.class);

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final IssueMapper issueMapper;

    public IssueQueryServiceImpl(
            IssueRepository issueRepository,
            UserRepository userRepository,
            IssueHistoryRepository issueHistoryRepository,
            IssueMapper issueMapper
    ) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.issueHistoryRepository = issueHistoryRepository;
        this.issueMapper = issueMapper;
    }

    // ========================
    // Citizen
    // ========================
    
    @Override
    public IssueResponse getIssueResponse(Integer issueId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        IssueResponse response = issueMapper.toResponse(issue);

        // =========================
        // 1️⃣ Assigned Official
        // =========================
        if (issue.getAssignedOfficialId() != null) {

            userRepository.findByIdAndActiveTrue(issue.getAssignedOfficialId())
                    .ifPresent(user -> {
                        response.setAssignedOfficialName(user.getFullName());
                        response.setAssignedOfficialMobile(user.getMobile());
                        response.setAssignedOfficialEmail(user.getEmail());
                    });
        }

        // =========================
        // 2️⃣ Ward Superior
        // =========================
        List<User> superiors =
                userRepository.findByRoleAndWardIdAndActiveTrue(
                        UserRole.WARD_SUPERIOR,
                        issue.getWardId()
                );

        if (!superiors.isEmpty()) {

            User superior = superiors.get(0);

            response.setWardSuperiorName(superior.getFullName());
            response.setWardSuperiorMobile(superior.getMobile());
            response.setWardSuperiorEmail(superior.getEmail());
        }

        // =========================
        // 3️⃣ History Timeline
        // =========================
        List<IssueHistoryResponse> historyList =
                issueHistoryRepository
                        .findByIssueIdOrderByChangedAtAsc(issueId)
                        .stream()
                        .map(history -> {
                            IssueHistoryResponse h = new IssueHistoryResponse();
                            h.setOldStatus(history.getOldStatus());
                            h.setNewStatus(history.getNewStatus());
                            h.setChangedAt(history.getChangedAt());
                            h.setRemarks(history.getRemarks());
                            return h;
                        })
                        .toList();

        response.setHistory(historyList);

        return response;
    }

   
    @Override
    public Issue getIssueById(Integer issueId) {

        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    @Override
    public List<Issue> getIssuesByReporter(Integer reporterId) {

        return issueRepository.findByReportedByOrderByCreatedAtDesc(reporterId);
    }

    // ========================
    // Admin
    // ========================

    @Override
    public List<Issue> getIssuesByWard(Integer wardId) {

        return issueRepository.findByWardId(wardId);
    }

    @Override
    public List<Issue> getIssuesByDepartment(Integer departmentId) {

        return issueRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Issue> getIssuesByReporterAdmin(Integer reporterId) {

        return issueRepository.findByReportedBy(reporterId);
    }

    @Override
    public List<Issue> getIssuesByStatus(IssueStatus status) {

        return issueRepository.findByStatus(status);
    }

    // ========================
    // Official
    // ========================

    @Override
    public List<Issue> getIssuesForOfficial(Integer wardId,
                                            Integer departmentId) {

        return issueRepository.findByWardIdAndDepartmentId(
                wardId,
                departmentId
        );
    }

    @Override
    public List<Issue> getIssuesForOfficialByStatus(
            Integer wardId,
            Integer departmentId,
            IssueStatus status) {

        return issueRepository.findByWardIdAndDepartmentIdAndStatus(
                wardId,
                departmentId,
                status
        );
    }

    // ========================
    // Ward Superior
    // ========================

    @Override
    public List<Issue> getIssuesByWardAndStatus(
            Integer wardId,
            IssueStatus status) {

        return issueRepository.findByWardIdAndStatus(
                wardId,
                status
        );
    }
    
    @Override
    public Page<Issue> filterIssues(
            Integer wardId,
            Integer departmentId,
            Integer reportedBy,
            IssueStatus status,
            UserRole role,
            Integer userWardId,
            Integer userDepartmentId,
            Pageable pageable
    ) {

        // ======================================
        // 🔒 ROLE ENFORCEMENT (STRICT VALIDATION)
        // ======================================

        Integer effectiveWardId = wardId;
        Integer effectiveDepartmentId = departmentId;

        if (role == UserRole.OFFICIAL) {

            if (userWardId == null || userDepartmentId == null) {
                throw new IllegalStateException("Official configuration invalid");
            }

            effectiveWardId = userWardId;
            effectiveDepartmentId = userDepartmentId;
        }

        else if (role == UserRole.WARD_SUPERIOR) {

            if (userWardId == null) {
                throw new IllegalStateException("Ward superior configuration invalid");
            }

            effectiveWardId = userWardId;
            // department optional
        }

        // ADMIN → no restriction

        // ======================================
        // 🛠 DEFAULT SORTING SAFETY
        // ======================================

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        // ======================================
        // 🔎 SPECIFICATION BUILD
        // ======================================

        Page<Issue> result = issueRepository.findAll(
                IssueSpecification
                        .isActive()
                        .and(IssueSpecification.hasWard(effectiveWardId))
                        .and(IssueSpecification.hasDepartment(effectiveDepartmentId))
                        .and(IssueSpecification.hasReporter(reportedBy))
                        .and(IssueSpecification.hasStatus(status)),
                pageable
        );

        // ======================================
        // 📊 ENTERPRISE AUDIT LOGGING
        // ======================================

        log.info(
                "Issue Filter Used | role={} | ward={} | dept={} | reporter={} | status={} | page={} | size={} | totalResults={}",
                role,
                effectiveWardId,
                effectiveDepartmentId,
                reportedBy,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                result.getTotalElements()
        );

        return result;
    }


}
