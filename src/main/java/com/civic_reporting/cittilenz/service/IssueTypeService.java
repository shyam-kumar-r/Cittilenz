package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.request.IssueTypeUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.entity.IssueType;

import java.util.List;

public interface IssueTypeService {

    /* ===== READ (DTO) ===== */
    List<IssueTypeResponse> getActiveIssueTypes();

    List<IssueTypeResponse> getActiveByDepartment(Integer departmentId);

    IssueTypeResponse getById(Integer id);

    /* ===== WRITE (ENTITY) ===== */
    IssueType create(IssueType issueType);

    IssueTypeResponse update(Integer id, IssueTypeUpdateRequest req);

    void changeStatus(Integer id, boolean active);
}
