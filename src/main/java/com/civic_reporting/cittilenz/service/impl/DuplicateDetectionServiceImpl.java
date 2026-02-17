package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.DuplicateDetectionService;
import com.civic_reporting.cittilenz.util.DuplicateSimilarityUtil;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionServiceImpl implements DuplicateDetectionService {
	
	private static final Logger log =
	        LoggerFactory.getLogger(DuplicateDetectionServiceImpl.class);


    private final IssueRepository issueRepository;

    public DuplicateDetectionServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Optional<Issue> findDuplicate(
            Integer wardId,
            Integer issueTypeId,
            Point location
    ) {

        double radius = DuplicateSimilarityUtil.getDuplicateRadiusMeters();

        return issueRepository.findDuplicateSpatial(
                wardId,
                issueTypeId,
                location,
                radius
        );
    }
}
