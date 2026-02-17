package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.service.IssueQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/issues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIssueController {

    private final IssueQueryService issueQueryService;
    private final IssueMapper issueMapper;

    public AdminIssueController(
            IssueQueryService issueQueryService,
            IssueMapper issueMapper
    ) {
        this.issueQueryService = issueQueryService;
        this.issueMapper = issueMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueResponse>>> filterIssues(
            @RequestParam(required = false) Integer wardId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer reportedBy,
            @RequestParam(required = false) IssueStatus status
    ) {

        List<IssueResponse> response =
                issueQueryService
                        .filterIssues(
                                wardId,
                                departmentId,
                                reportedBy,
                                status,
                                UserRole.ADMIN,
                                null,
                                null
                        )
                        .stream()
                        .map(issueMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
