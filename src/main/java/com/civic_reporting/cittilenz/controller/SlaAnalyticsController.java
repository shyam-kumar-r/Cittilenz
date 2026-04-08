package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.AnalyticsFilterRequest;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.SlaAnalyticsResponse;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.security.UserPrincipal;
import com.civic_reporting.cittilenz.service.SlaAnalyticsService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('ADMIN','WARD_SUPERIOR')")
public class SlaAnalyticsController {

    private final SlaAnalyticsService analyticsService;
    private final UserRepository userRepository;

    public SlaAnalyticsController(
            SlaAnalyticsService analyticsService,
            UserRepository userRepository
    ) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/sla")
    public ResponseEntity<ApiResponse<SlaAnalyticsResponse>> getSlaAnalytics() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "SLA analytics fetched",
                        analyticsService.getOverallAnalytics()
                )
        );
    }

    @PostMapping("/sla/filter")
    public ResponseEntity<ApiResponse<SlaAnalyticsResponse>> getFilteredAnalytics(
            @Valid @RequestBody AnalyticsFilterRequest filter
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Filtered SLA analytics fetched",
                        analyticsService.getFilteredAnalytics(filter)
                )
        );
    }

    @GetMapping("/last7")
    public ResponseEntity<ApiResponse<SlaAnalyticsResponse>> last7DaysAnalytics(
            @RequestParam(required = false) Integer wardId,
            @RequestParam(required = false) Integer departmentId,
            Authentication authentication
    ) {

        User currentUser = extractUser(authentication);
        Integer effectiveWardId = resolveWardScope(currentUser, wardId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Last 7 days analytics",
                        analyticsService.getLast7DaysAnalytics(
                                effectiveWardId, departmentId
                        )
                )
        );
    }

    @GetMapping("/last30")
    public ResponseEntity<ApiResponse<SlaAnalyticsResponse>> last30DaysAnalytics(
            @RequestParam(required = false) @Min(1) Integer wardId,
            @RequestParam(required = false) @Min(1) Integer departmentId,
            Authentication authentication
    ) {

        User currentUser = extractUser(authentication);
        Integer effectiveWardId = resolveWardScope(currentUser, wardId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Last 30 days analytics",
                        analyticsService.getLast30DaysAnalytics(
                                effectiveWardId, departmentId
                        )
                )
        );
    }

    private User extractUser(Authentication authentication) {

        if (authentication == null ||
            !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Unauthorized");
        }

        User user = principal.getUser();

        if (!user.isActive()) {
            throw new AccessDeniedException("User inactive");
        }

        return user;
    }

    private Integer resolveWardScope(User user, Integer requestedWardId) {

        if (UserRole.ADMIN.equals(user.getRole())) {
            return requestedWardId;
        }

        if (UserRole.WARD_SUPERIOR.equals(user.getRole())) {

            if (requestedWardId != null &&
                user.getWardId() != null &&
                !requestedWardId.equals(user.getWardId())) {

                throw new AccessDeniedException(
                    "Ward superior cannot access other wards");
            }

            return user.getWardId();
        }

        throw new AccessDeniedException("Unauthorized role");
    }
}