package com.civic_reporting.cittilenz.analytics;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.civic_reporting.cittilenz.dto.request.AnalyticsFilterRequest;

@Component
public class TimeRangeFactory {

    public AnalyticsFilterRequest last7Days(
            Integer wardId,
            Integer deptId
    ) {
        AnalyticsFilterRequest f = new AnalyticsFilterRequest();
        f.setWardId(wardId);
        f.setDepartmentId(deptId);
        f.setFromDate(LocalDateTime.now().minusDays(7));
        f.setToDate(LocalDateTime.now());
        return f;
    }

    public AnalyticsFilterRequest last30Days(
            Integer wardId,
            Integer deptId
    ) {
        AnalyticsFilterRequest f = new AnalyticsFilterRequest();
        f.setWardId(wardId);
        f.setDepartmentId(deptId);
        f.setFromDate(LocalDateTime.now().minusDays(30));
        f.setToDate(LocalDateTime.now());
        return f;
    }
}