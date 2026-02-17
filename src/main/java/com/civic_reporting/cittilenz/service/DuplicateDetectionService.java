package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.Issue;
import org.locationtech.jts.geom.Point;

import java.util.Optional;

public interface DuplicateDetectionService {

    Optional<Issue> findDuplicate(
            Integer wardId,
            Integer issueTypeId,
            Point location
    );
}
