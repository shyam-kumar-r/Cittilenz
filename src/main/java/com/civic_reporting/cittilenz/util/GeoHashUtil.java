package com.civic_reporting.cittilenz.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GeoHashUtil {

    private GeoHashUtil() {}

    /**
     * Creates normalized hash key for lat/lon.
     * Rounded to 6 decimal places to prevent micro-variance.
     */
    public static String createLatLonHash(Double latitude, Double longitude) {

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be null");
        }

        BigDecimal lat = BigDecimal.valueOf(latitude)
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal lon = BigDecimal.valueOf(longitude)
                .setScale(6, RoundingMode.HALF_UP);

        return lat.toPlainString() + "_" + lon.toPlainString();
    }
}
