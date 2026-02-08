package com.civic_reporting.cittilenz.controller;

import com.civic_reporting.cittilenz.mapper.WardMapper;
import com.civic_reporting.cittilenz.service.WardService;
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
    public List<?> getAll() {
        return wardService.getAllWards()
                .stream()
                .map(WardMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return WardMapper.toResponse(wardService.getWardById(id));
    }

    @GetMapping("/lookup")
    public Object lookupWard(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return WardMapper.toResponse(
                wardService.findWardByCoordinates(lat, lng)
        );
    }
}
