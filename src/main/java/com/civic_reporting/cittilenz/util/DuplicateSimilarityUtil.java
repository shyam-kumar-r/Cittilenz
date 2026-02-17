package com.civic_reporting.cittilenz.util;

public class DuplicateSimilarityUtil {

    // 30 meters threshold
    private static final double DUPLICATE_RADIUS_METERS = 30.0;

    private DuplicateSimilarityUtil() {}

    /**
     * Returns duplicate detection radius in meters.
     */
    public static double getDuplicateRadiusMeters() {
        return DUPLICATE_RADIUS_METERS;
    }
}
