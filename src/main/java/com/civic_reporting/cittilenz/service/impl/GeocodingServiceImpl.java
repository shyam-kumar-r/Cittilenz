package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.client.NominatimClient;
import com.civic_reporting.cittilenz.entity.GeocodeCache;
import com.civic_reporting.cittilenz.repository.GeocodeCacheRepository;
import com.civic_reporting.cittilenz.service.GeocodingService;
import com.civic_reporting.cittilenz.util.GeoHashUtil;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final Logger log =
            LoggerFactory.getLogger(GeocodingServiceImpl.class);

    private final GeocodeCacheRepository geocodeCacheRepository;
    private final NominatimClient nominatimClient;
    private final RateLimiter rateLimiter;

    public GeocodingServiceImpl(
            GeocodeCacheRepository geocodeCacheRepository,
            NominatimClient nominatimClient,
            RateLimiter nominatimRateLimiter
    ) {
        this.geocodeCacheRepository = geocodeCacheRepository;
        this.nominatimClient = nominatimClient;
        this.rateLimiter = nominatimRateLimiter;
    }

    @Override
    public Map<String, String> reverseGeocode(Double latitude, Double longitude) {

        try {

            String hash = GeoHashUtil.createLatLonHash(latitude, longitude);

            // 1️⃣ CACHE FIRST
            Optional<GeocodeCache> cached =
                    geocodeCacheRepository.findByLatLonHash(hash);

            if (cached.isPresent()) {
                return mapFromCache(cached.get());
            }

            // 2️⃣ RATE LIMIT ENFORCEMENT
            Supplier<Map> decoratedSupplier =
                    RateLimiter.decorateSupplier(rateLimiter,
                            () -> nominatimClient.reverseGeocode(latitude, longitude));

            Map response = decoratedSupplier.get();

            if (response == null || response.get("address") == null) {
                log.warn("Geocoding returned empty response.");
                return null;
            }

            Map address = (Map) response.get("address");
            
            log.info("Reverse geocoding response: {}", address);

            Map<String, String> result = extractAddress(address);

            saveToCache(hash, latitude, longitude, result);

            return result;

        } catch (Exception ex) {
            // 3️⃣ FAIL SAFE — NEVER BREAK ISSUE CREATION
            log.warn("Geocoding failed. Continuing without enrichment.", ex);
            return null;
        }
    }

    private Map<String, String> extractAddress(Map address) {

        Map<String, String> result = new HashMap<>();

        result.put("street", (String) address.get("road"));
        result.put("area", (String) address.get("suburb"));
        result.put("locality", (String) address.get("city_district"));
        result.put("city", (String) address.get("city"));
        result.put("pincode", (String) address.get("postcode"));
        result.put("state", (String) address.get("state"));
        result.put("country", (String) address.get("country"));

        return result;
    }

    private Map<String, String> mapFromCache(GeocodeCache geo) {

        Map<String, String> result = new HashMap<>();
        result.put("street", geo.getStreet());
        result.put("area", geo.getArea());
        result.put("locality", geo.getLocality());
        result.put("city", geo.getCity());
        result.put("pincode", geo.getPincode());
        result.put("state", geo.getState());
        result.put("country", geo.getCountry());

        return result;
    }

    private void saveToCache(String hash,
                             Double lat,
                             Double lon,
                             Map<String, String> data) {

        GeocodeCache cache = new GeocodeCache();
        cache.setLat(lat);
        cache.setLon(lon);
        cache.setLatLonHash(hash);
        cache.setStreet(data.get("street"));
        cache.setArea(data.get("area"));
        cache.setLocality(data.get("locality"));
        cache.setCity(data.get("city"));
        cache.setPincode(data.get("pincode"));
        cache.setState(data.get("state"));
        cache.setCountry(data.get("country"));
        cache.setCreatedAt(LocalDateTime.now());

        try {
            geocodeCacheRepository.save(cache);
        } catch (Exception ignored) {
            // Race condition safe
        }
    }
}
