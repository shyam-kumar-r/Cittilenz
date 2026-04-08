package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.service.IssueTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issue-types")
public class IssueTypeController {

    private final IssueTypeService service;

    public IssueTypeController(IssueTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueTypeResponse>>> list(
            @RequestParam(required = false) Integer departmentId) {

        List<IssueTypeResponse> data = (departmentId == null)
                ? service.getActiveIssueTypes()
                : service.getActiveByDepartment(departmentId);

        return ResponseEntity.ok(
        	    ApiResponse.success("Issue types fetched successfully", data)
        	);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueTypeResponse>> get(@PathVariable Integer id) {

        return ResponseEntity.ok(
                ApiResponse.success("Issue type fetched", service.getById(id))
        );
    }
}