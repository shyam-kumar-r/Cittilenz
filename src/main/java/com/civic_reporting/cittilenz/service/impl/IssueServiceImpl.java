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
import java.util.Map;
import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {
	
	private static final Logger log =
	        LoggerFactory.getLogger(IssueServiceImpl.class);


    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final IssueImageRepository issueImageRepository;
    private final IssueReporterRepository issueReporterRepository;
    private final WardRepository wardRepository;
    private final IssueEnrichmentService issueEnrichmentService;



    private final GeocodingService geocodingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    private final IssueHistoryRepository issueHistoryRepository;
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final IssueTypeRepository issueTypeRepository;

    private static final String LOGO_URL =
    "https://raw.githubusercontent.com/shyam-kumar-r/Cittilenz/master/src/main/resources/static/logo.jpeg";


    public IssueServiceImpl(
            UserRepository userRepository,
            IssueRepository issueRepository,
            IssueImageRepository issueImageRepository,
            IssueReporterRepository issueReporterRepository,
            WardRepository wardRepository,
            GeocodingService geocodingService,
            DuplicateDetectionService duplicateDetectionService,
            AssignmentService assignmentService,
            FileStorageService fileStorageService,
            IssueEnrichmentService issueEnrichmentService,
            IssueHistoryRepository issueHistoryRepository,
            NotificationService notificationService,
            TemplateService templateService,
            IssueTypeRepository issueTypeRepository
    ) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.issueImageRepository = issueImageRepository;
        this.issueReporterRepository = issueReporterRepository;
        this.wardRepository = wardRepository;
        this.geocodingService = geocodingService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.assignmentService = assignmentService;
        this.fileStorageService = fileStorageService;
        this.issueEnrichmentService = issueEnrichmentService;
        this.issueHistoryRepository = issueHistoryRepository;
        this.notificationService = notificationService;
        this.templateService = templateService;
        this.issueTypeRepository = issueTypeRepository;
    }

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
    public Issue createIssue(
            IssueCreateRequest request,
            MultipartFile image,
            Integer reporterId
    ) {
    	
    	log.info("Create issue started | reporterId={}", reporterId);

        // 1️⃣ Validate Reporter
        User reporter = userRepository.findByIdAndActiveTrue(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        if (reporter.getRole() != UserRole.CITIZEN) {
            throw new IllegalArgumentException("Only citizens can report issues");
        }

        // 2️⃣ AI Classification
        if (request.getIssueTypeId() == null) {
            throw new IllegalArgumentException("Issue type is required");
        }
        
        IssueType issueType = issueTypeRepository
                .findById(request.getIssueTypeId())
                .filter(IssueType::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive issue type"));
        
        Integer departmentId = issueType.getDepartment().getId();
        
        String departmentName = issueType.getDepartment().getName();
        
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("Location (latitude and longitude coordinates) is required");
        }

        // 3️⃣ Location
        Point location = GeometryUtil.createPoint(
                request.getLatitude(),
                request.getLongitude()
        );

        // 4️⃣ Ward
        Ward ward = wardRepository.findWardContainingPoint(location)
                .orElseThrow(() -> new ResourceNotFoundException("Location outside service area"));

        Integer wardId = ward.getId();
        String wardName = ward.getWardName();

        // 5️⃣ Duplicate
        Optional<Issue> duplicate =
                duplicateDetectionService.findDuplicate(
                        wardId,
                        issueType.getId(),
                        location
                );

        if (duplicate.isPresent()) {

            Issue existing = duplicate.get();
            existing.setReportCount(existing.getReportCount() + 1);
            issueRepository.save(existing);

            if (issueReporterRepository
                    .findByIssueIdAndUserId(existing.getId(), reporterId)
                    .isEmpty()) {

                IssueReporter map = new IssueReporter();
                map.setIssueId(existing.getId());
                map.setUserId(reporterId);
                map.setReportedAt(LocalDateTime.now());
                issueReporterRepository.save(map);
            }

            notificationService.notifyUser(
                    reporterId,
                    "Issue Linked Successfully",
                    "ISSUE_LINKED",
                    "ISSUE_LINKED",
                    existing.getId()
            );

            return existing;
        }

        // 6️⃣ Assignment
        Optional<User> assigned =
                assignmentService.assignOfficial(wardId, departmentId);

        Issue issue = new Issue();

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setLocation(location);

        issue.setIssueTypeId(issueType.getId());
        issue.setIssueTypeName(issueType.getName());
        issue.setDepartmentId(departmentId);
        issue.setDepartmentName(departmentName);

        issue.setWardId(wardId);
        issue.setWardName(wardName);

        issue.setReportedBy(reporterId);
        issue.setReportedByName(reporter.getFullName());

        issue.setPriority(issueType.getPriority());
        issue.setCreatedAt(LocalDateTime.now());
        issue.setStatus(IssueStatus.SUBMITTED);

        issue.setSoftSlaBreached(false);
        issue.setHardSlaBreached(false);
        issue.setReassignmentCount(0);
        issue.setEscalationCount(0);
        issue.setRequiresSupervisorIntervention(false);
        issue.setActive(true);
        
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        String imageUrl = fileStorageService.storeFile(image);
        issue.setImageUrl(imageUrl);

        if (assigned.isPresent()) {
            Integer officialId = assigned.get().getId();
            issue.setAssignedOfficialId(officialId);
            issue.setAssignedAt(LocalDateTime.now());
            issue.setStatus(IssueStatus.ASSIGNED);
        }

        // ✅ SAVE FIRST (CRITICAL)
        Issue saved = issueRepository.save(issue);

        // ================= NOTIFICATIONS (CORRECT ORDER) =================

        // CREATED
        notificationService.notifyUser(
                reporterId,
                "Issue Reported Successfully",
                "ISSUE_CREATED",
                "ISSUE_CREATED",
                saved.getId()
        );

        // ASSIGNED (if applicable)
        if (assigned.isPresent()) {

            Integer officialId = assigned.get().getId();

            notificationService.notifyUser(
                    reporterId,
                    "Issue Assigned",
                    "ISSUE_ASSIGNED",
                    "ISSUE_ASSIGNED",
                    saved.getId()
            );

            notificationService.notifyUser(
                    officialId,
                    "New Issue Assigned",
                    "ISSUE_ASSIGNED",
                    "ISSUE_ASSIGNED",
                    saved.getId()
            );
        }

        // ================= HISTORY =================

        IssueHistory creation = new IssueHistory();
        creation.setIssueId(saved.getId());
        creation.setOldStatus(null);
        creation.setNewStatus(IssueStatus.SUBMITTED);
        creation.setChangedBy(reporterId);
        creation.setRemarks("Issue created");
        creation.setChangedAt(LocalDateTime.now());
        issueHistoryRepository.save(creation);

        if (assigned.isPresent()) {

            IssueHistory assign = new IssueHistory();
            assign.setIssueId(saved.getId());
            assign.setOldStatus(IssueStatus.SUBMITTED);
            assign.setNewStatus(IssueStatus.ASSIGNED);
            assign.setChangedBy(assigned.get().getId());
            assign.setRemarks("Issue assigned");
            assign.setChangedAt(LocalDateTime.now());

            issueHistoryRepository.save(assign);
        }

        // ================= ASYNC =================

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        issueEnrichmentService.enrichIssueAddress(saved.getId());
                    }
                }
        );

        // ================= IMAGE =================

        IssueImage img = new IssueImage();
        img.setIssueId(saved.getId());
        img.setImageUrl(saved.getImageUrl());
        img.setImageType("REPORTED");
        img.setUploadedBy(reporterId);
        img.setUploadedAt(LocalDateTime.now());
        issueImageRepository.save(img);

        // ================= REPORTER =================

        IssueReporter rep = new IssueReporter();
        rep.setIssueId(saved.getId());
        rep.setUserId(reporterId);
        rep.setReportedAt(LocalDateTime.now());
        issueReporterRepository.save(rep);
        
        log.info("Issue created successfully | issueId={}", saved.getId());
        
        return saved;
    }




    @Override
    public Issue getIssueById(Integer issueId,
                              Integer viewerId,
                              UserRole role) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        // Visibility check
        if (role == UserRole.CITIZEN &&
                !issue.getReportedBy().equals(viewerId)) {

            throw new IllegalArgumentException("Unauthorized access");
        }

        return issue;
    }

    @Override
    public List<Issue> getIssuesByReporter(Integer reporterId) {

        return issueRepository.findByReportedByOrderByCreatedAtDesc(reporterId);
    }
    
    @Override
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

        // Check if already linked
        if (issueReporterRepository
                .findByIssueIdAndUserId(issueId, reporterId)
                .isPresent()) {

            return issue; // already linked
        }

        // Increment report count
        issue.setReportCount(issue.getReportCount() + 1);
        issueRepository.save(issue);

        // Insert mapping
        IssueReporter issueReporter = new IssueReporter();
        issueReporter.setIssueId(issueId);
        issueReporter.setUserId(reporterId);
        issueReporter.setReportedAt(LocalDateTime.now());

        issueReporterRepository.save(issueReporter);
        return issue;
        
    }
}
