package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.Ward;

import java.util.List;

public interface WardService {

    List<Ward> getAllWards();
    Ward getWardById(Integer id);
    Ward findWardByCoordinates(double latitude, double longitude);
}
