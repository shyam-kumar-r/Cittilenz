package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import com.civic_reporting.cittilenz.dto.response.WardResponse;
import com.civic_reporting.cittilenz.mapper.WardMapper;
import com.civic_reporting.cittilenz.service.WardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WardResponse>>> getAll() {

        List<WardResponse> list = wardService.getAllWards()
                .stream()
                .map(WardMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Wards fetched", list)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WardResponse>> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        WardMapper.toResponse(wardService.getWardById(id))
                )
        );
    }

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<WardResponse>> lookupWard(
            @RequestParam double lat,
            @RequestParam double lng
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ward lookup successful",
                        WardMapper.toResponse(
                                wardService.findWardByCoordinates(lat, lng)
                        )
                )
        );
    }
}