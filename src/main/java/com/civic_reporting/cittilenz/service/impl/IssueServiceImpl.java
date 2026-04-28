package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.IssueCreateRequest;
import com.civic_reporting.cittilenz.entity.*;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.repository.*;
import com.civic_reporting.cittilenz.service.*;
import com.civic_reporting.cittilenz.util.GeometryUtil;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final IssueReporterRepository issueReporterRepository;
    private final IssueImageRepository issueImageRepository;

    private final AssignmentService assignmentService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final IssueEnrichmentService issueEnrichmentService;
    
    private static final Logger log =
	        LoggerFactory.getLogger(IssueServiceImpl.class);
    
    public IssueServiceImpl(
            UserRepository userRepository,
            IssueRepository issueRepository,
            IssueImageRepository issueImageRepository,
            IssueReporterRepository issueReporterRepository,
            WardRepository wardRepository,
            DuplicateDetectionService duplicateDetectionService,
            AssignmentService assignmentService,
            FileStorageService fileStorageService,
            IssueEnrichmentService issueEnrichmentService,
            IssueHistoryRepository issueHistoryRepository,
            NotificationService notificationService,
            IssueTypeRepository issueTypeRepository
    ) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.issueImageRepository = issueImageRepository;
        this.issueReporterRepository = issueReporterRepository;
        this.wardRepository = wardRepository;
        this.duplicateDetectionService = duplicateDetectionService;
        this.assignmentService = assignmentService;
        this.fileStorageService = fileStorageService;
        this.issueEnrichmentService = issueEnrichmentService;
        this.issueHistoryRepository = issueHistoryRepository;
        this.notificationService = notificationService;
        this.issueTypeRepository = issueTypeRepository;
    }
    // =========================================================
    // CREATE ISSUE (FINAL VERSION)
    // =========================================================
    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "issueFilterCache",
                    "adminDashboardCache",
                    "citizenDashboardCache",
                    "officialDashboardCache",
                    "superiorDashboardCache"
            },
            allEntries = true
    )
    public Issue createIssue(IssueCreateRequest request,
                             MultipartFile image,
                             Integer reporterId) {

        log.info("Create issue started | reporterId={}", reporterId);

        // ================= VALIDATE REPORTER =================
        User reporter = userRepository.findByIdAndActiveTrue(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        if (reporter.getRole() != UserRole.CITIZEN) {
            throw new IllegalArgumentException("Only citizens can report issues");
        }

        // ================= VALIDATE ISSUE TYPE =================
        Integer issueTypeId = request.getIssueTypeId();
        if (issueTypeId == null) {
            throw new IllegalArgumentException("Issue type is required");
        }

        IssueType issueType = issueTypeRepository.findById(issueTypeId)
                .filter(IssueType::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid issue type"));

        Integer departmentId = issueType.getDepartment().getId();
        String departmentName = issueType.getDepartment().getName();

        // ================= VALIDATE LOCATION =================
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Location is required");
        }

        Point location = GeometryUtil.createPoint(lat, lng);

        Ward ward = wardRepository.findWardContainingPoint(location)
                .orElseThrow(() -> new ResourceNotFoundException("Outside service area"));

        Integer wardId = ward.getId();
        String wardName = ward.getWardName();

        // ================= DUPLICATE DETECTION =================
        Optional<Issue> duplicate =
                duplicateDetectionService.findDuplicate(wardId, issueTypeId, location);

        if (duplicate.isPresent()) {

            Issue existing = duplicate.get();

            existing.setReportCount(existing.getReportCount() + 1);
            issueRepository.save(existing);

            issueReporterRepository
                    .findByIssueIdAndUserId(existing.getId(), reporterId)
                    .orElseGet(() -> {
                        IssueReporter map = new IssueReporter();
                        map.setIssueId(existing.getId());
                        map.setUserId(reporterId);
                        map.setReportedAt(LocalDateTime.now());
                        return issueReporterRepository.save(map);
                    });

            notificationService.notifyUser(
                    reporterId,
                    "Issue Linked Successfully",
                    "ISSUE_LINKED",
                    "ISSUE_LINKED",
                    existing.getId()
            );

            return existing;
        }

        // ================= IMAGE =================
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        String imageUrl = fileStorageService.storeFile(image);

        // ================= ASSIGNMENT =================
        Optional<User> assigned =
                assignmentService.assignOfficial(wardId, departmentId);

        // ================= CREATE ISSUE =================
        Issue issue = new Issue();

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());

        issue.setLatitude(lat);
        issue.setLongitude(lng);
        issue.setLocation(location);

        issue.setIssueTypeId(issueTypeId);
        issue.setIssueTypeName(issueType.getName());

        issue.setDepartmentId(departmentId);
        issue.setDepartmentName(departmentName);

        issue.setWardId(wardId);
        issue.setWardName(wardName);

        // 🔥 SNAPSHOT
        issue.setReportedBy(reporterId);
        issue.setReportedByName(reporter.getFullName());

        issue.setPriority(issueType.getPriority());
        issue.setCreatedAt(LocalDateTime.now());
        issue.setImageUrl(imageUrl);
        issue.setStatus(IssueStatus.SUBMITTED);

        if (assigned.isPresent()) {

            User official = assigned.get();

            issue.setAssignedOfficialId(official.getId());
            issue.setAssignedOfficialName(official.getFullName());
            issue.setAssignedOfficialEmail(official.getEmail());

            issue.setAssignedAt(LocalDateTime.now());
            issue.setStatus(IssueStatus.ASSIGNED);

        } else {

            issue.setAssignedOfficialId(null);
            issue.setAssignedOfficialName(null);
            issue.setAssignedOfficialEmail(null);

            issue.setStatus(IssueStatus.UNASSIGNED);
        }

        Issue saved = issueRepository.save(issue);

        // ================= HISTORY =================
        IssueHistory history = new IssueHistory();
        history.setIssueId(saved.getId());
        history.setOldStatus(null);
        history.setNewStatus(saved.getStatus());
        history.setChangedBy(reporterId);
        history.setChangedByName(reporter.getFullName());
        history.setRemarks("Issue created");
        history.setChangedAt(LocalDateTime.now());

        issueHistoryRepository.save(history);

        // ================= IMAGE TRACKING =================
        IssueImage img = new IssueImage();
        img.setIssueId(saved.getId());
        img.setImageUrl(imageUrl);
        img.setImageType("REPORTED");

        img.setUploadedBy(reporterId);
        img.setUploadedByName(reporter.getFullName());

        img.setUploadedAt(LocalDateTime.now());

        issueImageRepository.save(img);

        // ================= REPORTER TRACK =================
        IssueReporter rep = new IssueReporter();
        rep.setIssueId(saved.getId());
        rep.setUserId(reporterId);
        rep.setReportedAt(LocalDateTime.now());

        issueReporterRepository.save(rep);

        // ================= NOTIFICATIONS =================
        notificationService.notifyUser(
                reporterId,
                "Issue Reported Successfully",
                "ISSUE_CREATED",
                "ISSUE_CREATED",
                saved.getId()
        );

        assigned.ifPresent(official -> {

            notificationService.notifyUser(
                    reporterId,
                    "Issue Assigned",
                    "ISSUE_ASSIGNED",
                    "ISSUE_ASSIGNED",
                    saved.getId()
            );

            notificationService.notifyUser(
                    official.getId(),
                    "New Issue Assigned",
                    "ISSUE_ASSIGNED",
                    "ISSUE_ASSIGNED",
                    saved.getId()
            );
        });

        // ================= ASYNC =================
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        issueEnrichmentService.enrichIssueAddress(saved.getId());
                    }
                }
        );

        log.info("Issue created successfully | issueId={}", saved.getId());

        return saved;
    }

    // =========================================================
    // GET ISSUE BY ID (SAFE AFTER DELETE)
    // =========================================================
    @Override
    public Issue getIssueById(Integer issueId,
                             Integer viewerId,
                             UserRole role) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        if (role == UserRole.CITIZEN &&
                (issue.getReportedBy() == null || !issue.getReportedBy().equals(viewerId))) {
            throw new IllegalArgumentException("Unauthorized access");
        }

        return issue;
    }

    // =========================================================
    // GET ISSUES BY REPORTER
    // =========================================================
    @Override
    public List<Issue> getIssuesByReporter(Integer reporterId) {

        return issueRepository.findByReportedByOrderByCreatedAtDesc(reporterId);
    }

    // =========================================================
    // LINK DUPLICATE
    // =========================================================
    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "issueFilterCache",
                    "adminDashboardCache",
                    "citizenDashboardCache",
                    "officialDashboardCache",
                    "superiorDashboardCache"
            },
            allEntries = true
    )
    public Issue linkDuplicate(Integer issueId, Integer reporterId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User reporter = userRepository.findByIdAndActiveTrue(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        if (reporter.getRole() != UserRole.CITIZEN) {
            throw new IllegalArgumentException("Only citizens can link duplicates");
        }

        if (issueReporterRepository
                .findByIssueIdAndUserId(issueId, reporterId)
                .isPresent()) {
            return issue;
        }

        issue.setReportCount(issue.getReportCount() + 1);
        issueRepository.save(issue);

        IssueReporter map = new IssueReporter();
        map.setIssueId(issueId);
        map.setUserId(reporterId);
        map.setReportedAt(LocalDateTime.now());

        issueReporterRepository.save(map);

        return issue;
    }
}