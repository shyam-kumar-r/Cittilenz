package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.IssueQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<List<IssueResponse>>> filterOfficialIssues(
            @RequestParam(required = false) Integer reportedBy,
            @RequestParam(required = false) IssueStatus status,
            Authentication authentication
    ) {

        User official = userRepository
                .findByUsernameAndActiveTrue(authentication.getName())
                .orElseThrow();

        List<IssueResponse> response =
                issueQueryService
                        .filterIssues(
                                null,                           // ward filter ignored
                                null,                           // dept filter ignored
                                reportedBy,
                                status,
                                UserRole.OFFICIAL,
                                official.getWardId(),
                                official.getDepartmentId()
                        )
                        .stream()
                        .map(issueMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
