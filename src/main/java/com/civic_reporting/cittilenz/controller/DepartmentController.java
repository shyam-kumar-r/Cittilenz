package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.mapper.DepartmentMapper;
import com.civic_reporting.cittilenz.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<?> getAll() {
        return service.getAll()
                .stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return DepartmentMapper.toResponse(service.getById(id));
    }
}
