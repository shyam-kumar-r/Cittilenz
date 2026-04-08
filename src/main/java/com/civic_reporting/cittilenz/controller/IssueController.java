package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.IssueCreateRequest;
import com.civic_reporting.cittilenz.dto.response.*;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.*;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;
    private final IssueQueryService issueQueryService;
    private final IssueMapper issueMapper;
    private final UserRepository userRepository;
    private final DashboardAnalyticsService dashboardAnalyticsService;

    public IssueController(
            IssueService issueService,
            IssueQueryService issueQueryService,
            IssueMapper issueMapper,
            UserRepository userRepository,
            DashboardAnalyticsService dashboardAnalyticsService
    ) {
        this.issueService = issueService;
        this.issueQueryService = issueQueryService;
        this.issueMapper = issueMapper;
        this.userRepository = userRepository;
        this.dashboardAnalyticsService = dashboardAnalyticsService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @Valid @ModelAttribute IssueCreateRequest request,
            @RequestParam("image") MultipartFile image,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Issue issue = issueService.createIssue(request, image, user.getId());

        IssueResponse response =
                issueQueryService.getIssueResponse(issue.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Issue created successfully", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssue(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        issueService.getIssueById(id, user.getId(), user.getRole());

        IssueResponse response = issueQueryService.getIssueResponse(id);

        return ResponseEntity.ok(
                ApiResponse.success("Issue fetched successfully", response)
        );
    }

    @RateLimiter(name = "issueFilterLimiter")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getMyIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueResponse> response = issueQueryService
                .getIssuesByReporter(user.getId(), pageable)
                .map(issueMapper::toResponse);

        return ResponseEntity.ok(
                ApiResponse.success("User issues fetched", response)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<CitizenDashboardResponse>> getDashboard(
            Authentication authentication
    ) {

        User citizen = getCurrentUser(authentication);

        CitizenDashboardResponse response =
                dashboardAnalyticsService.getCitizenDashboard(citizen.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard fetched", response)
        );
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<IssueResponse>> linkDuplicate(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Issue issue = issueService.linkDuplicate(id, user.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Duplicate linked successfully",
                        issueMapper.toResponse(issue))
        );
    }

    private User getCurrentUser(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Unauthorized");
        }

        return userRepository
                .findByUsernameAndActiveTrue(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}