package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.*;
import com.civic_reporting.cittilenz.dto.response.IssueResponse;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.mapper.IssueMapper;
import com.civic_reporting.cittilenz.security.UserPrincipal;
import com.civic_reporting.cittilenz.service.IssueLifecycleService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/issues")
public class IssueLifecycleController {

    private final IssueLifecycleService lifecycleService;
    private final IssueMapper issueMapper;

    public IssueLifecycleController(
            IssueLifecycleService lifecycleService,
            IssueMapper issueMapper
    ) {
        this.lifecycleService = lifecycleService;
        this.issueMapper = issueMapper;
    }

    @PreAuthorize("hasRole('OFFICIAL')")
    @PostMapping("/{id}/start")
    public IssueResponse startWork(
            @PathVariable Integer id,
            @Valid @RequestBody StartWorkRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Issue updated = lifecycleService.startWork(
                id,
                user.getId(),
                request.getVersion()
        );

        return issueMapper.toResponse(updated);
    }

    @PreAuthorize("hasRole('OFFICIAL')")
    @PostMapping(value = "/{id}/resolve", consumes = "multipart/form-data")
    public IssueResponse resolveIssue(
            @PathVariable Integer id,
            @RequestParam("version") Long version,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Issue updated = lifecycleService.resolveIssue(
                id,
                user.getId(),
                version,
                image
        );

        return issueMapper.toResponse(updated);
    }

    @PreAuthorize("hasRole('WARD_SUPERIOR')")
    @PostMapping("/{id}/reassign")
    public IssueResponse reassignIssue(
            @PathVariable Integer id,
            @Valid @RequestBody ReassignIssueRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Issue updated = lifecycleService.reassignEscalatedIssue(
                id,
                user.getId(),
                request.getVersion()
        );

        return issueMapper.toResponse(updated);
    }

    @PreAuthorize("hasRole('WARD_SUPERIOR')")
    @PostMapping("/{id}/supervisor-reassign")
    public IssueResponse supervisorReassign(
            @PathVariable Integer id,
            @Valid @RequestBody ReassignIssueRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Issue updated = lifecycleService.supervisorReassignSoftBreached(
                id,
                user.getId(),
                request.getVersion()
        );

        return issueMapper.toResponse(updated);
    }

    @PreAuthorize("hasRole('WARD_SUPERIOR')")
    @PostMapping("/{id}/supervisor-clear")
    public IssueResponse supervisorClear(
            @PathVariable Integer id,
            @Valid @RequestBody SupervisorClearRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Issue updated = lifecycleService.supervisorClearIntervention(
                id,
                user.getId(),
                request.getVersion(),
                request.getRemarks()
        );

        return issueMapper.toResponse(updated);
    }
}