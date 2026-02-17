package com.civic_reporting.cittilenz.util;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtil {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private GeometryUtil() {}

    /**
     * Creates a PostGIS Point (SRID 4326).
     * IMPORTANT: longitude comes first.
     */
    public static Point createPoint(Double latitude, Double longitude) {

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be null");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude range");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude range");
        }

        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);

        return point;
    }
}
