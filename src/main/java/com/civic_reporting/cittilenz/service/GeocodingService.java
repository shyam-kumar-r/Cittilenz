package com.civic_reporting.cittilenz.service;

import java.util.Map;

public interface GeocodingService {

    Map<String, String> reverseGeocode(Double latitude, Double longitude);
}
