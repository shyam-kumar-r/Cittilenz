package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.IssueHistory;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.repository.IssueHistoryRepository;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.FileStorageService;
import com.civic_reporting.cittilenz.service.IssueLifecycleService;
import com.civic_reporting.cittilenz.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IssueLifecycleServiceImpl implements IssueLifecycleService {

    private static final Logger log =
            LoggerFactory.getLogger(IssueLifecycleServiceImpl.class);

    private final IssueRepository issueRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public IssueLifecycleServiceImpl(
            IssueRepository issueRepository,
            IssueHistoryRepository issueHistoryRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService
    ) {
        this.issueRepository = issueRepository;
        this.issueHistoryRepository = issueHistoryRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    // ======================================================
    // CENTRAL STATE MACHINE
    // ======================================================

    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    IssueStatus.SUBMITTED, Set.of(IssueStatus.ASSIGNED),

                    IssueStatus.ASSIGNED, Set.of(IssueStatus.IN_PROGRESS),

                    IssueStatus.IN_PROGRESS, Set.of(
                            IssueStatus.RESOLVED,
                            IssueStatus.ESCALATED
                    ),

                    IssueStatus.ESCALATED, Set.of(IssueStatus.REASSIGNED),

                    IssueStatus.REASSIGNED, Set.of(IssueStatus.ASSIGNED),

                    IssueStatus.RESOLVED, Set.of()
            );

    // ======================================================
    // START WORK
    // ASSIGNED → IN_PROGRESS
    // ======================================================

    @Override
    @Transactional
    public Issue startWork(Integer issueId,
                           Integer officialId,
                           Long version) {

        Issue issue = fetchIssue(issueId);

        // Optimistic lock validation
        validateVersion(issue.getVersion(), version);

        User official = validateOfficial(officialId);

        if (!officialId.equals(issue.getAssignedOfficialId())) {
            throw new AccessDeniedException("Official not assigned to issue");
        }

        IssueStatus oldStatus = issue.getStatus();

        // Validate state transition
        validateTransition(oldStatus, IssueStatus.IN_PROGRESS);

        // State mutation
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issue.setStartedAt(LocalDateTime.now());

        // Persist
        Issue saved = issueRepository.save(issue);

        // Structured log AFTER persistence
        log.info(
            "Issue {} transitioned {} -> {} by official {}",
            saved.getId(),
            oldStatus,
            IssueStatus.IN_PROGRESS,
            officialId
        );

        // Write audit history
        writeHistory(
                saved.getId(),
                oldStatus,
                IssueStatus.IN_PROGRESS,
                officialId,
                "Official started work"
        );
        
        Integer reporterId = saved.getReportedBy();
        Integer assignedOfficial = saved.getAssignedOfficialId();

        notificationService.notifyUser(reporterId,
                "Issue In Progress", "ISSUE_IN_PROGRESS", "ISSUE_IN_PROGRESS", saved.getId());

        notificationService.notifyUser(assignedOfficial,
                "Work Started on Issue", "ISSUE_IN_PROGRESS", "ISSUE_IN_PROGRESS",saved.getId());

        return saved;
        
    }

    // ======================================================
    // RESOLVE ISSUE
    // IN_PROGRESS → RESOLVED
    // ======================================================

    @Override
    @Transactional
    public Issue resolveIssue(Integer issueId,
                              Integer officialId,
                              Long version,
                              MultipartFile image) {

        Issue issue = fetchIssue(issueId);

        validateVersion(issue.getVersion(), version);

        User official = validateOfficial(officialId);

        if (!officialId.equals(issue.getAssignedOfficialId())) {
            throw new AccessDeniedException("Official not assigned");
        }

        // 🔴 CRITICAL VALIDATIONS
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Resolution image is required");
        }

        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        IssueStatus oldStatus = issue.getStatus();

        validateTransition(oldStatus, IssueStatus.RESOLVED);

        // 🔥 STORE IMAGE
        String imagePath = fileStorageService.storeFile(image);

        // 🔥 UPDATE STATE
        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolvedAt(LocalDateTime.now());
        issue.setResolvedImageUrl(imagePath);

        Issue saved = issueRepository.save(issue);

        String officialName = getUserName(officialId);

        String autoRemark =
                "The issue '" + issue.getTitle() +
                "' has been resolved by the official " + officialName;

        writeHistory(
                saved.getId(),
                oldStatus,
                IssueStatus.RESOLVED,
                officialId,
                autoRemark
        );
        
        notificationService.notifyUser(saved.getReportedBy(),
                "Issue Resolved", "ISSUE_RESOLVED", "ISSUE_RESOLVED", saved.getId());

        notificationService.notifyUser(saved.getAssignedOfficialId(),
                "Issue Closed", "ISSUE_RESOLVED", "ISSUE_RESOLVED", saved.getId());
        
        return saved;
    }
    // ======================================================
    // SUPERVISOR REASSIGNMENT
    // ESCALATED → REASSIGNED
    // OR
    // ASSIGNED → ASSIGNED (after soft breach)
    // ======================================================

    @Override
    @Transactional
    public Issue reassignEscalatedIssue(
            Integer issueId,
            Integer superiorId,
            Long version
    ) {

        Issue issue = fetchIssue(issueId);

        validateVersion(issue.getVersion(), version);

        User superior = validateSupervisor(superiorId);

        if (!issue.getWardId().equals(superior.getWardId())) {
            throw new AccessDeniedException("Ward mismatch");
        }

        if (issue.getStatus() != IssueStatus.ESCALATED) {
            throw new IllegalStateException("Only escalated issues can be reassigned");
        }

        Integer newOfficialId = userRepository
                .findSmartOfficialForAssignment(
                        issue.getWardId(),
                        issue.getDepartmentId(),
                        issue.getAssignedOfficialId(),
                        issue.getId(),
                        3
                )
                .orElseThrow(() -> new IllegalStateException("No eligible official found"));

        IssueStatus oldStatus = issue.getStatus();

        Integer failedOfficialId = issue.getAssignedOfficialId();

        issue.setAssignedOfficialId(newOfficialId);
        issue.setStatus(IssueStatus.REASSIGNED);
        issue.setReassignedAt(LocalDateTime.now());
        issue.setSoftSlaBreached(false);
        issue.setStartedAt(null);
        issue.setHardSlaDeadline(null);
        issue.setHardSlaBreached(false);
        issue.setRequiresSupervisorIntervention(false);

        Issue saved = issueRepository.save(issue);

        String failedOfficial = getUserName(failedOfficialId);
        String newOfficial = getUserName(newOfficialId);

        String autoRemark =
                "Hard SLA breached by official " + failedOfficial +
                " for the issue '" + issue.getTitle() +
                "'. Issue is reassigned to official " + newOfficial;

        writeHistory(
                saved.getId(),
                oldStatus,
                IssueStatus.REASSIGNED,
                superiorId,
                autoRemark
        );
        
        notificationService.notifyUser(saved.getReportedBy(),
                "Escalated Issue Processed", "Issue has been Reassigned due to Delay.", "SLA_HARD_ESCALATION_PROCESSED", saved.getId());

        notificationService.notifyUser(newOfficialId,
                "Escalated Issue Assigned", "Escalated Issue has been Assigned by the Superior.", "SLA_HARD_ESCALATION_PROCESSED", saved.getId());

        notificationService.notifyUser(superiorId,
                "Escalation Processed", "Reviewed and Reassigned Issue.", "SLA_HARD_ESCALATION_PROCESSED", saved.getId());

        return saved;
    }
    // ======================================================
    // SUPERVISOR REASSIGN AFTER SOFT BREACH
    // ======================================================

    @Override
    @Transactional
    public Issue supervisorReassignSoftBreached(
            Integer issueId,
            Integer superiorId,
            Long version
    ) {

        Issue issue = fetchIssue(issueId);

        validateVersion(issue.getVersion(), version);

        User superior = validateSupervisor(superiorId);

        if (!issue.isRequiresSupervisorIntervention()) {
            throw new IllegalStateException("Supervisor intervention not required");
        }

        Integer newOfficialId = userRepository
                .findSmartOfficialForAssignment(
                        issue.getWardId(),
                        issue.getDepartmentId(),
                        issue.getAssignedOfficialId(),
                        issue.getId(),
                        3
                )
                .orElseThrow(() -> new IllegalStateException("No eligible official found"));

        LocalDateTime now = LocalDateTime.now();

        issue.setAssignedOfficialId(newOfficialId);
        issue.setReassignmentCount(0);
        issue.setSoftSlaBreached(false);
        issue.setRequiresSupervisorIntervention(false);
        issue.setStartedAt(null);
        issue.setHardSlaDeadline(null);
        issue.setHardSlaBreached(false);
        issue.setRequiresSupervisorIntervention(false);

        Issue saved = issueRepository.save(issue);

        List<String> failedOfficials = getLastFailedOfficials(issue.getId(), 2);

        String autoRemark =
                "Soft SLA breached by officials " +
                String.join(", ", failedOfficials) +
                " for the issue '" + issue.getTitle() + "'";

        writeHistory(
                saved.getId(),
                IssueStatus.ASSIGNED,
                IssueStatus.ASSIGNED,
                superiorId,
                autoRemark
        );
        
        notificationService.notifyUser(saved.getReportedBy(),
                "Issue Reassigned", "SLA_SOFT_BREACH", "SLA_SOFT_BREACH", saved.getId());

        notificationService.notifyUser(newOfficialId,
                "New Issue Assigned", "SLA_REASSIGNED", "SLA_REASSIGNED", saved.getId());

        notificationService.notifyUser(superiorId,
                "Supervisor Intervention", "SLA_SUPERVISOR_ALERT", "SLA_SUPERVISOR_ALERT", saved.getId());

        return saved;
    }

    // ======================================================
    // SUPERVISOR CLEAR INTERVENTION
    // ======================================================

    @Override
    @Transactional
    public Issue supervisorClearIntervention(
            Integer issueId,
            Integer superiorId,
            Long version,
            String remarks
    ) {

        Issue issue = fetchIssue(issueId);

        validateVersion(issue.getVersion(), version);

        User superior = validateSupervisor(superiorId);

        if (!issue.getWardId().equals(superior.getWardId())) {
            throw new AccessDeniedException("Ward mismatch");
        }

        if (!issue.isRequiresSupervisorIntervention()) {
            throw new IllegalStateException("Intervention not required");
        }

        // Governance action
        issue.setRequiresSupervisorIntervention(false);

        // Restart soft SLA timer
        issue.setAssignedAt(LocalDateTime.now());

        Issue saved = issueRepository.save(issue);

        // Structured governance log
        log.info(
            "Supervisor {} cleared intervention for issue {}",
            superiorId,
            saved.getId()
        );

        // Governance history entry
        writeHistory(
                saved.getId(),
                IssueStatus.ASSIGNED,
                IssueStatus.ASSIGNED,
                superiorId,
                remarks != null ? remarks : "Supervisor cleared intervention"
        );

        return saved;
    }

    // ======================================================
    // VALIDATION HELPERS
    // ======================================================

    private Issue fetchIssue(Integer issueId) {

        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    private User validateOfficial(Integer officialId) {

        User official = userRepository.findById(officialId)
                .orElseThrow(() -> new ResourceNotFoundException("Official not found"));

        if (official.getRole() != UserRole.OFFICIAL)
            throw new AccessDeniedException("User not official");

        if (!official.isActive())
            throw new IllegalStateException("Official inactive");

        return official;
    }

    private User validateSupervisor(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found"));

        if (user.getRole() != UserRole.WARD_SUPERIOR)
            throw new AccessDeniedException("Only ward superior allowed");

        return user;
    }

    private void validateVersion(Long current, Long incoming) {

    	if (incoming == null || !incoming.equals(current)) {
    	    throw new IllegalStateException("Version conflict. Refresh required.");
    	}
    }

    private void validateTransition(IssueStatus current, IssueStatus next) {

        Set<IssueStatus> allowed = ALLOWED_TRANSITIONS.get(current);

        if (allowed == null || !allowed.contains(next)) {

            throw new IllegalStateException(
                    "Invalid lifecycle transition: " + current + " → " + next
            );
        }
    }

    // ======================================================
    // HISTORY
    // ======================================================

    private void writeHistory(Integer issueId,
                              IssueStatus oldStatus,
                              IssueStatus newStatus,
                              Integer changedBy,
                              String remarks) {

        IssueHistory history = new IssueHistory();

        history.setIssueId(issueId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setRemarks(remarks);
        history.setChangedAt(LocalDateTime.now());

        issueHistoryRepository.save(history);
    }

    private void logTransition(Integer issueId,
                               IssueStatus oldStatus,
                               IssueStatus newStatus,
                               Integer actorId) {

        log.info("Issue {} transitioned {} → {} by user {}",
                issueId, oldStatus, newStatus, actorId);
    }
    
    private String getUserName(Integer userId) {
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse("Unknown");
    }
    
    private List<String> getLastFailedOfficials(Integer issueId, int limit) {
        return issueHistoryRepository
                .findTopOfficialsForIssue(issueId, limit)
                .stream()
                .map(id -> getUserName(id))
                .toList();
    }
}