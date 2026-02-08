package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.DepartmentResponse;
import com.civic_reporting.cittilenz.entity.Department;

public class DepartmentMapper {

    public static DepartmentResponse toResponse(Department d) {
        DepartmentResponse r = new DepartmentResponse();
        r.id = d.getId();
        r.name = d.getName();
        r.description = d.getDescription();
        return r;
    }
}
