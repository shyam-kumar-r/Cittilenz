package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.AdminDashboardResponse;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.DashboardAnalyticsResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.service.DashboardAnalyticsService;
import com.civic_reporting.cittilenz.service.IssueQueryService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/issues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIssueController {

    private final IssueQueryService issueQueryService;
    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final IssueMapper issueMapper;

    public AdminIssueController(
            IssueQueryService issueQueryService,
            IssueMapper issueMapper,
            DashboardAnalyticsService dashboardAnalyticsService
    ) {
        this.issueQueryService = issueQueryService;
        this.issueMapper = issueMapper;
        this.dashboardAnalyticsService = dashboardAnalyticsService;
    }
    
    @RateLimiter(name = "issueFilterLimiter")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> filterIssues(

            @RequestParam(required = false) Integer wardId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer reportedBy,
            @RequestParam(required = false) IssueStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<Issue> pageResult =
                issueQueryService.filterIssues(
                        wardId,
                        departmentId,
                        reportedBy,
                        status,
                        UserRole.ADMIN,
                        null,
                        null,
                        pageable
                );

        Page<IssueResponse> responsePage =
                pageResult.map(issueMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {

        AdminDashboardResponse response =
                dashboardAnalyticsService.getAdminDashboard();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
