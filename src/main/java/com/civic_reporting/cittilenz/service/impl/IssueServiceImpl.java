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


    private final AiClassificationService aiClassificationService;
    private final GeocodingService geocodingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    private final IssueHistoryRepository issueHistoryRepository;


    public IssueServiceImpl(
            UserRepository userRepository,
            IssueRepository issueRepository,
            IssueImageRepository issueImageRepository,
            IssueReporterRepository issueReporterRepository,
            WardRepository wardRepository,
            AiClassificationService aiClassificationService,
            GeocodingService geocodingService,
            DuplicateDetectionService duplicateDetectionService,
            AssignmentService assignmentService,
            FileStorageService fileStorageService,
            IssueEnrichmentService issueEnrichmentService,
            IssueHistoryRepository issueHistoryRepository

    ) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.issueImageRepository = issueImageRepository;
        this.issueReporterRepository = issueReporterRepository;
        this.wardRepository = wardRepository;
        this.aiClassificationService = aiClassificationService;
        this.geocodingService = geocodingService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.assignmentService = assignmentService;
        this.fileStorageService = fileStorageService;
        this.issueEnrichmentService = issueEnrichmentService;
        this.issueHistoryRepository = issueHistoryRepository;

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

        // 1️⃣ Validate Reporter
        User reporter = userRepository.findByIdAndActiveTrue(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        if (reporter.getRole() != UserRole.CITIZEN) {
            throw new IllegalArgumentException("Only citizens can report issues");
        }

        // 2️⃣ AI Classification
        IssueType issueType = aiClassificationService.classifyIssue(image);

        Integer departmentId = issueType.getDepartment().getId();
        String departmentName = issueType.getDepartment().getName();

        // 3️⃣ Geometry Creation
        Point location = GeometryUtil.createPoint(
                request.getLatitude(),
                request.getLongitude()
        );

        // 4️⃣ Ward Lookup
        Ward ward = wardRepository.findWardContainingPoint(location)
                .orElseThrow(() -> new ResourceNotFoundException("Location outside service area"));

        Integer wardId = ward.getId();
        String wardName = ward.getWardName();

        // 5️⃣ Duplicate Detection
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

                IssueReporter issueReporter = new IssueReporter();
                issueReporter.setIssueId(existing.getId());
                issueReporter.setUserId(reporterId);
                issueReporter.setReportedAt(LocalDateTime.now());

                issueReporterRepository.save(issueReporter);
            }

            return existing;
        }

        // 6️⃣ Assignment (before saving)
        Optional<User> assigned =
                assignmentService.assignOfficial(wardId, departmentId);

        Issue issue = new Issue();

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setLocation(location);

        issue.setIssueTypeId(issueType.getId());
        issue.setDepartmentId(departmentId);
        issue.setDepartmentName(departmentName);

        issue.setWardId(wardId);
        issue.setWardName(wardName);

        issue.setReportedBy(reporterId);
        issue.setReportedByName(reporter.getFullName());

        issue.setPriority(issueType.getPriority());
        issue.setCreatedAt(LocalDateTime.now());
        issue.setStatus(IssueStatus.SUBMITTED); // default first state
        issue.setSoftSlaBreached(false);
        issue.setHardSlaBreached(false);

        issue.setReassignmentCount(0);
        issue.setEscalationCount(0);

        issue.setRequiresSupervisorIntervention(false);
        issue.setActive(true);

        String imageUrl = fileStorageService.storeFile(image);
        issue.setImageUrl(imageUrl);

        // If assigned → mark assigned
        if (assigned.isPresent()) {
            issue.setAssignedOfficialId(assigned.get().getId());
            issue.setAssignedAt(LocalDateTime.now());
            issue.setStatus(IssueStatus.ASSIGNED);
        }

        Issue saved = issueRepository.save(issue);

        // ===============================
        // 7️⃣ Lifecycle History Logging
        // ===============================

        // First record (creation)
        IssueHistory creationHistory = new IssueHistory();
        creationHistory.setIssueId(saved.getId());
        creationHistory.setOldStatus(null);
        creationHistory.setNewStatus(IssueStatus.SUBMITTED);
        creationHistory.setChangedBy(reporterId);
        creationHistory.setRemarks("Issue created");
        creationHistory.setChangedAt(LocalDateTime.now());

        issueHistoryRepository.save(creationHistory);

        // If assigned immediately → second record
        if (assigned.isPresent()) {

            IssueHistory assignmentHistory = new IssueHistory();
            assignmentHistory.setIssueId(saved.getId());
            assignmentHistory.setOldStatus(IssueStatus.SUBMITTED);
            assignmentHistory.setNewStatus(IssueStatus.ASSIGNED);
            assignmentHistory.setChangedBy(assigned.get().getId());
            assignmentHistory.setRemarks("Issue assigned to official");
            assignmentHistory.setChangedAt(LocalDateTime.now());

            issueHistoryRepository.save(assignmentHistory);
        }

     // ===============================
     // 8️⃣ Async Address Enrichment (After Commit Only)
     // ===============================

     TransactionSynchronizationManager.registerSynchronization(
             new TransactionSynchronization() {
                 @Override
                 public void afterCommit() {
                     issueEnrichmentService.enrichIssueAddress(saved.getId());
                 }
             }
     );

        // ===============================
        // 9️⃣ Save Issue Image Mapping
        // ===============================
        IssueImage issueImage = new IssueImage();
        issueImage.setIssueId(saved.getId());
        issueImage.setImageUrl(saved.getImageUrl());
        issueImage.setImageType("REPORTED");
        issueImage.setUploadedBy(reporterId);
        issueImage.setUploadedAt(LocalDateTime.now());

        issueImageRepository.save(issueImage);

        // ===============================
        // 🔟 Save Reporter Mapping
        // ===============================
        IssueReporter issueReporter = new IssueReporter();
        issueReporter.setIssueId(saved.getId());
        issueReporter.setUserId(reporterId);
        issueReporter.setReportedAt(LocalDateTime.now());

        issueReporterRepository.save(issueReporter);

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
