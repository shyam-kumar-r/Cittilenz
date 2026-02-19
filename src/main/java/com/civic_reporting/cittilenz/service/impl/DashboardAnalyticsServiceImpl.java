package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.response.*;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.DashboardAnalyticsService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public DashboardAnalyticsServiceImpl(
            IssueRepository issueRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // 🟣 ADMIN DASHBOARD
    // =====================================================

    @Override
    @Cacheable(cacheNames = "adminDashboardCache")
    public AdminDashboardResponse getAdminDashboard() {

        AdminDashboardResponse response = new AdminDashboardResponse();

        response.setTotalCitizens(userRepository.countActiveCitizens());
        response.setTotalOfficials(userRepository.countActiveOfficials());
        response.setTotalWardSuperiors(userRepository.countActiveWardSuperiors());
        response.setTotalIssues(issueRepository.count());

        return response;
    }

    // =====================================================
    // 🔵 CITIZEN DASHBOARD
    // =====================================================

    @Override
    @Cacheable(
    	    cacheNames = "citizenDashboardCache",
    	    key = "'citizen:' + #userId"
    	)
    public CitizenDashboardResponse getCitizenDashboard(Integer userId) {

        CitizenDashboardResponse response = new CitizenDashboardResponse();

        Map<IssueStatus, Long> statusMap =
                mapStatusCounts(issueRepository.countByStatusCitizen(userId));

        long totalReported = statusMap.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        response.setTotalReported(totalReported);
        response.setTotalResolved(statusMap.getOrDefault(IssueStatus.RESOLVED, 0L));
        response.setTotalAssignedOrInProgress(
                statusMap.getOrDefault(IssueStatus.ASSIGNED, 0L)
                        + statusMap.getOrDefault(IssueStatus.IN_PROGRESS, 0L)
        );
        response.setTotalEscalated(statusMap.getOrDefault(IssueStatus.ESCALATED, 0L));

        return response;
    }

    // =====================================================
    // 🟢 OFFICIAL DASHBOARD
    // =====================================================

    @Override
    @Cacheable(
    	    cacheNames = "officialDashboardCache",
    	    key = "'official:' + #officialId"
    	)
    public OfficialDashboardResponse getOfficialDashboard(Integer officialId) {

        OfficialDashboardResponse response = new OfficialDashboardResponse();

        Map<IssueStatus, Long> statusMap =
                mapStatusCounts(issueRepository.countByStatusOfficial(officialId));

        response.setTotalAssigned(statusMap.getOrDefault(IssueStatus.ASSIGNED, 0L));
        response.setTotalInProgress(statusMap.getOrDefault(IssueStatus.IN_PROGRESS, 0L));
        response.setTotalResolved(statusMap.getOrDefault(IssueStatus.RESOLVED, 0L));
        response.setTotalEscalated(statusMap.getOrDefault(IssueStatus.ESCALATED, 0L));

        return response;
    }

    // =====================================================
    // 🟠 WARD SUPERIOR DASHBOARD
    // =====================================================

    @Override
    @Cacheable(
    	    cacheNames = "superiorDashboardCache",
    	    key = "'superior:' + #wardId"
    	)
    public WardSuperiorDashboardResponse getWardSuperiorDashboard(Integer wardId) {

        WardSuperiorDashboardResponse response = new WardSuperiorDashboardResponse();

        Map<IssueStatus, Long> statusMap =
                mapStatusCounts(issueRepository.countEscalatedByWard(wardId));

        response.setTotalEscalated(
                statusMap.getOrDefault(IssueStatus.ESCALATED, 0L)
        );

        return response;
    }

    // =====================================================
    // 🔧 COMMON MAPPER
    // =====================================================

    private Map<IssueStatus, Long> mapStatusCounts(List<Object[]> raw) {

        Map<IssueStatus, Long> result = new HashMap<>();

        for (Object[] row : raw) {
            IssueStatus status = (IssueStatus) row[0];
            Long count = (Long) row[1];
            result.put(status, count);
        }

        return result;
    }
}
