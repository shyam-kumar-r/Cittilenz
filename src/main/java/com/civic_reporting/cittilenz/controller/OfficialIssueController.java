package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.IssueQueryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/official/issues")
@PreAuthorize("hasRole('OFFICIAL')")
public class OfficialIssueController {

    private final IssueQueryService issueQueryService;
    private final IssueMapper issueMapper;
    private final UserRepository userRepository;

    public OfficialIssueController(
            IssueQueryService issueQueryService,
            IssueMapper issueMapper,
            UserRepository userRepository
    ) {
        this.issueQueryService = issueQueryService;
        this.issueMapper = issueMapper;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> filterOfficialIssues(

            @RequestParam(required = false) Integer reportedBy,
            @RequestParam(required = false) IssueStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            Authentication authentication
    ) {

        User official = userRepository
                .findByUsernameAndActiveTrue(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Official not found"));

        if (official.getWardId() == null || official.getDepartmentId() == null) {
            throw new IllegalStateException("Official configuration invalid");
        }

        Page<Issue> pageResult =
                issueQueryService.filterIssues(
                        null,                                // ward filter ignored (auto enforced)
                        null,                                // dept filter ignored (auto enforced)
                        reportedBy,
                        status,
                        UserRole.OFFICIAL,
                        official.getWardId(),
                        official.getDepartmentId(),
                        pageable
                );

        Page<IssueResponse> responsePage =
                pageResult.map(issueMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
}
