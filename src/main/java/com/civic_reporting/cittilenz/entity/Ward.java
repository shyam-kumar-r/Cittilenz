package com.civic_reporting.cittilenz.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "wards")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ward_number", nullable = false, unique = true)
    private Integer wardNumber;

    @Column(name = "ward_name", nullable = false)
    private String wardName;

    @Column(
        columnDefinition = "geometry(MULTIPOLYGON,4326)",
        nullable = false
    )
    private MultiPolygon boundary;

    // getters
    public Integer getId() { return id; }
    public Integer getWardNumber() { return wardNumber; }
    public String getWardName() { return wardName; }
    public MultiPolygon getBoundary() { return boundary; }
}
