package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.entity.IssueType;

public class IssueTypeMapper {

    public static IssueTypeResponse toResponse(IssueType it) {
        IssueTypeResponse r = new IssueTypeResponse();
        r.id = it.getId();
        r.name = it.getName();
        r.slaHours = it.getSlaHours();
        r.priority = it.getPriority().name();
        r.active = it.isActive();

        r.departmentId = it.getDepartment().getId();
        r.departmentName = it.getDepartment().getName(); // SAFE NOW

        r.description = it.getDescription();
        return r;
    }
}

