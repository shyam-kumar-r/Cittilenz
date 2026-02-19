package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.response.AdminDashboardResponse;
import com.civic_reporting.cittilenz.dto.response.CitizenDashboardResponse;
import com.civic_reporting.cittilenz.dto.response.OfficialDashboardResponse;
import com.civic_reporting.cittilenz.dto.response.WardSuperiorDashboardResponse;

public interface DashboardAnalyticsService {

    AdminDashboardResponse getAdminDashboard();

    CitizenDashboardResponse getCitizenDashboard(Integer userId);

    OfficialDashboardResponse getOfficialDashboard(Integer officialId);

    WardSuperiorDashboardResponse getWardSuperiorDashboard(Integer wardId);
}

