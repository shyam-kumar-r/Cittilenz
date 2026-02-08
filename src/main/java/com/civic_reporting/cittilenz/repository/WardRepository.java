package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.Ward;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Integer> {

    @Query("""
        SELECT w
        FROM Ward w
        WHERE ST_Contains(w.boundary, :point) = true
    """)
    Optional<Ward> findWardByPoint(Point point);
}
