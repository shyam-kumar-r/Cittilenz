package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Ward;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.repository.WardRepository;
import com.civic_reporting.cittilenz.service.WardService;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WardServiceImpl implements WardService {

    private final WardRepository wardRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public WardServiceImpl(WardRepository wardRepository) {
        this.wardRepository = wardRepository;
    }

    @Override
    public List<Ward> getAllWards() {
        return wardRepository.findAll();
    }

    @Override
    public Ward getWardById(Integer id) {
        return wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
    }

    @Override
    public Ward findWardByCoordinates(double latitude, double longitude) {

        validateCoordinates(latitude, longitude);

        // IMPORTANT:
        // POINT(x, y) = POINT(longitude, latitude)
        Point point = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );
        point.setSRID(4326);

        return wardRepository.findWardByPoint(point)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No ward found for given coordinates"
                        ));
    }

    private void validateCoordinates(double lat, double lng) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Invalid latitude value");
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Invalid longitude value");
        }
    }
}
