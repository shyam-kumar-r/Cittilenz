package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.DepartmentResponse;
import com.civic_reporting.cittilenz.mapper.DepartmentMapper;
import com.civic_reporting.cittilenz.service.DepartmentService;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll() {

        List<DepartmentResponse> list = service.getAll()
                .stream()
                .map(DepartmentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched successfully", list)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        DepartmentMapper.toResponse(service.getById(id))
                )
        );
    }
}