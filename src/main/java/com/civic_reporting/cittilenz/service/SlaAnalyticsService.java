package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.request.AnalyticsFilterRequest;
import com.civic_reporting.cittilenz.dto.response.SlaAnalyticsResponse;

public interface SlaAnalyticsService {
    SlaAnalyticsResponse getOverallAnalytics();
    
    SlaAnalyticsResponse getFilteredAnalytics(AnalyticsFilterRequest filter);
    
    SlaAnalyticsResponse getLast7DaysAnalytics(Integer wardId, Integer departmentId);
    
    SlaAnalyticsResponse getLast30DaysAnalytics(Integer wardId, Integer departmentId);
}