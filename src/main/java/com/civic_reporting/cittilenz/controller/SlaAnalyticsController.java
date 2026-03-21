package com.civic_reporting.cittilenz.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.civic_reporting.cittilenz.dto.request.AnalyticsFilterRequest;
import com.civic_reporting.cittilenz.dto.response.SlaAnalyticsResponse;
import com.civic_reporting.cittilenz.service.SlaAnalyticsService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.Min;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;
import com.civic_reporting.cittilenz.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;



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
    public SlaAnalyticsResponse getSlaAnalytics() {
        return analyticsService.getOverallAnalytics();
    }
    
    @PostMapping("/sla/filter")
    public SlaAnalyticsResponse getFilteredAnalytics(
            @RequestBody AnalyticsFilterRequest filter
    ) {
        return analyticsService.getFilteredAnalytics(filter);
    }
    
 // =========================================================
    // LAST 7 DAYS
    // =========================================================

    @GetMapping("/last7")
    public SlaAnalyticsResponse last7DaysAnalytics(
            @RequestParam(required = false) @Min(1) Integer wardId,
            @RequestParam(required = false) @Min(1) Integer departmentId,
            Authentication authentication
    ) {

        User currentUser = extractUser(authentication);

        Integer effectiveWardId = resolveWardScope(currentUser, wardId);

        return analyticsService.getLast7DaysAnalytics(
                effectiveWardId,
                departmentId
        );
    }

    // =========================================================
    // LAST 30 DAYS
    // =========================================================

    @GetMapping("/last30")
    public SlaAnalyticsResponse last30DaysAnalytics(
            @RequestParam(required = false) @Min(1) Integer wardId,
            @RequestParam(required = false) @Min(1) Integer departmentId,
            Authentication authentication
    ) {

        User currentUser = extractUser(authentication);

        Integer effectiveWardId = resolveWardScope(currentUser, wardId);

        return analyticsService.getLast30DaysAnalytics(
                effectiveWardId,
                departmentId
        );
    }

    // =========================================================
    // HELPER: Extract Authenticated User
    // =========================================================

    private User extractUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new AccessDeniedException("User not found or inactive"));
    }

    // =========================================================
    // HELPER: Ward Restriction Logic
    // =========================================================

    private Integer resolveWardScope(User user, Integer requestedWardId) {

        if (user.getRole() == UserRole.ADMIN) {
            // Admin can view any ward or all wards
            return requestedWardId;
        }

        if (user.getRole() == UserRole.WARD_SUPERIOR) {

            if (requestedWardId != null &&
                !requestedWardId.equals(user.getWardId())) {

                throw new AccessDeniedException(
                        "Ward superior cannot access other wards"
                );
            }

            // Force ward to their own ward
            return user.getWardId();
        }

        throw new AccessDeniedException("Unauthorized role");
    }
}