package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.IssueCreateRequest;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.IssueQueryService;
import com.civic_reporting.cittilenz.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;
    private final IssueQueryService issueQueryService;
    private final IssueMapper issueMapper;
    private final UserRepository userRepository;

    public IssueController(
            IssueService issueService,
            IssueQueryService issueQueryService,
            IssueMapper issueMapper,
            UserRepository userRepository
    ) {
        this.issueService = issueService;
        this.issueQueryService = issueQueryService;
        this.issueMapper = issueMapper;
        this.userRepository = userRepository;
    }

    // =========================
    // POST /issues
    // =========================
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @Valid @ModelAttribute IssueCreateRequest request,
            @RequestParam("image") MultipartFile image,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Issue issue = issueService.createIssue(
                request,
                image,
                user.getId()
        );

        IssueResponse response =
                issueQueryService.getIssueResponse(issue.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Issue created successfully", response)
        );
    }

    // =========================
    // GET /issues/{id}
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssue(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        issueService.getIssueById(
                id,
                user.getId(),
                user.getRole()
        );

        IssueResponse response =
                issueQueryService.getIssueResponse(id);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // =========================
    // GET /issues/my
    // =========================
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getMyIssues(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        List<IssueResponse> response = issueQueryService
                .getIssuesByReporter(user.getId())
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // =========================
    // POST /issues/{id}/duplicate
    // =========================
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<IssueResponse>> linkDuplicate(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Issue issue = issueService.linkDuplicate(id, user.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Duplicate linked", issueMapper.toResponse(issue))
        );
    }

    private User getCurrentUser(Authentication authentication) {

        String username = authentication.getName();

        return userRepository.findByIdAndActiveTrue(
                userRepository.findAll().stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .orElseThrow().getId()
        ).orElseThrow();
    }
}
