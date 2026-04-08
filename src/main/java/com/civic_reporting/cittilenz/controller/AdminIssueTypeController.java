package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.request.IssueTypeCreateRequest;
import com.civic_reporting.cittilenz.dto.request.IssueTypeUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.entity.Department;
import com.civic_reporting.cittilenz.entity.IssueType;
import com.civic_reporting.cittilenz.enums.IssuePriority;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.mapper.IssueTypeMapper;
import com.civic_reporting.cittilenz.repository.DepartmentRepository;
import com.civic_reporting.cittilenz.service.IssueTypeService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/issue-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIssueTypeController {

    private final IssueTypeService service;
    private final DepartmentRepository departmentRepository;

    public AdminIssueTypeController(IssueTypeService service,
                                    DepartmentRepository departmentRepository) {
        this.service = service;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueTypeResponse>> create(
            @Valid @RequestBody IssueTypeCreateRequest req) {

        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        IssueType it = new IssueType();
        it.setName(req.getName());
        it.setDepartment(dept);
        it.setSlaHours(req.getSlaHours());
        it.setPriority(IssuePriority.valueOf(req.getPriority()));
        it.setDescription(req.getDescription());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Issue type created successfully",
                        IssueTypeMapper.toResponse(service.create(it))
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueTypeResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody IssueTypeUpdateRequest req) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Issue type updated successfully",
                        service.update(id, req)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Integer id,
            @RequestParam boolean active) {

        service.changeStatus(id, active);

        return ResponseEntity.ok(
                ApiResponse.success("Status updated successfully", null)
        );
    }
}