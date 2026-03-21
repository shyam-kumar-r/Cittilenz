package com.civic_reporting.cittilenz.service.impl;

import org.springframework.stereotype.Service;

import com.civic_reporting.cittilenz.dto.projection.SlaAggregateProjection;
import com.civic_reporting.cittilenz.dto.request.AnalyticsFilterRequest;
import com.civic_reporting.cittilenz.dto.response.SlaAnalyticsResponse;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.SlaAnalyticsService;
import com.civic_reporting.cittilenz.analytics.TimeRangeFactory;

@Service
public class SlaAnalyticsServiceImpl implements SlaAnalyticsService {

    private final IssueRepository issueRepository;
    private final TimeRangeFactory timeRangeFactory;

    public SlaAnalyticsServiceImpl(
            IssueRepository issueRepository,
            TimeRangeFactory timeRangeFactory
    ) {
        this.issueRepository = issueRepository;
        this.timeRangeFactory = timeRangeFactory;
    }

    // =========================================================
    // EXISTING METHOD (UNCHANGED)
    // =========================================================

    @Override
    public SlaAnalyticsResponse getOverallAnalytics() {

        long total = issueRepository.countAllIssues();
        long assigned = issueRepository.countAssignedIssues();
        long inProgress = issueRepository.countInProgressIssues();
        long resolved = issueRepository.countResolvedIssues();
        long escalated = issueRepository.countEscalatedIssues();

        long softBreaches = issueRepository.countSoftBreaches();
        long hardBreaches = issueRepository.countHardBreaches();
        long supervisorFlag = issueRepository.countSupervisorInterventions();

        Double avgAck = issueRepository.averageAcknowledgementMinutes();
        Double avgRes = issueRepository.averageResolutionMinutes();

        long escalatedOnce = issueRepository.countEscalatedAtLeastOnce();
        long reassignedOnce = issueRepository.countReassignedAtLeastOnce();

        SlaAnalyticsResponse response = new SlaAnalyticsResponse();

        response.setTotalIssues(total);
        response.setAssignedIssues(assigned);
        response.setInProgressIssues(inProgress);
        response.setResolvedIssues(resolved);
        response.setEscalatedIssues(escalated);

        response.setSoftSlaBreaches(softBreaches);
        response.setHardSlaBreaches(hardBreaches);
        response.setSupervisorInterventionRequired(supervisorFlag);

        response.setAverageAcknowledgementMinutes(avgAck != null ? avgAck : 0);
        response.setAverageResolutionMinutes(avgRes != null ? avgRes : 0);

        response.setEscalationRatePercentage(
                total > 0 ? (double) escalatedOnce * 100 / total : 0
        );

        response.setReassignmentRatePercentage(
                total > 0 ? (double) reassignedOnce * 100 / total : 0
        );

        response.setSlaCompliancePercentage(
                total > 0 ? (double) (resolved - hardBreaches) * 100 / total : 0
        );

        return response;
    }

    // =========================================================
    // EXISTING FILTERED METHOD (UNCHANGED)
    // =========================================================

    @Override
    public SlaAnalyticsResponse getFilteredAnalytics(
            AnalyticsFilterRequest filter) {

        SlaAggregateProjection p =
                issueRepository.getAggregatedAnalytics(
                        filter.getWardId(),
                        filter.getDepartmentId(),
                        filter.getFromDate(),
                        filter.getToDate()
                );

        SlaAnalyticsResponse response = new SlaAnalyticsResponse();

        long total = safeLong(p.getTotal());

        response.setTotalIssues(total);
        response.setAssignedIssues(safeLong(p.getAssigned()));
        response.setInProgressIssues(safeLong(p.getInProgress()));
        response.setResolvedIssues(safeLong(p.getResolved()));
        response.setEscalatedIssues(safeLong(p.getEscalated()));

        response.setSoftSlaBreaches(safeLong(p.getSoftBreached()));
        response.setHardSlaBreaches(safeLong(p.getHardBreached()));
        response.setSupervisorInterventionRequired(
                safeLong(p.getSupervisorRequired())
        );

        response.setAverageAcknowledgementMinutes(
                safeDouble(p.getAvgAckMinutes())
        );

        response.setAverageResolutionMinutes(
                safeDouble(p.getAvgResolutionMinutes())
        );

        response.setEscalationRatePercentage(
                total > 0 ? safeLong(p.getEscalatedOnce()) * 100.0 / total : 0
        );

        response.setReassignmentRatePercentage(
                total > 0 ? safeLong(p.getReassignedOnce()) * 100.0 / total : 0
        );

        response.setSlaCompliancePercentage(
                total > 0 ?
                (safeLong(p.getResolved()) - safeLong(p.getHardBreached())) * 100.0 / total
                : 0
        );

        return response;
    }

    // =========================================================
    // NEW: LAST 7 DAYS ANALYTICS
    // =========================================================

    @Override
    public SlaAnalyticsResponse getLast7DaysAnalytics(
            Integer wardId,
            Integer departmentId
    ) {
        AnalyticsFilterRequest filter =
                timeRangeFactory.last7Days(wardId, departmentId);

        return getFilteredAnalytics(filter);
    }

    // =========================================================
    // NEW: LAST 30 DAYS ANALYTICS
    // =========================================================

    @Override
    public SlaAnalyticsResponse getLast30DaysAnalytics(
            Integer wardId,
            Integer departmentId
    ) {
        AnalyticsFilterRequest filter =
                timeRangeFactory.last30Days(wardId, departmentId);

        return getFilteredAnalytics(filter);
    }

    // =========================================================
    // INTERNAL SAFETY METHODS
    // =========================================================

    private long safeLong(Long value) {
        return value != null ? value : 0;
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }
}