package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.mapper.IssueTypeMapper;
import com.civic_reporting.cittilenz.service.IssueTypeService;
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
    public List<IssueTypeResponse> list(
            @RequestParam(required = false) Integer departmentId) {

        return (departmentId == null)
                ? service.getActiveIssueTypes()
                : service.getActiveByDepartment(departmentId);
    }

    @GetMapping("/{id}")
    public IssueTypeResponse get(@PathVariable Integer id) {
        return service.getById(id);
    }

}
