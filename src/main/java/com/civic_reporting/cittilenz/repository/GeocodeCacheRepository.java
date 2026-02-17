package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.GeocodeCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeocodeCacheRepository extends JpaRepository<GeocodeCache, Integer> {

    Optional<GeocodeCache> findByLatLonHash(String latLonHash);
}
