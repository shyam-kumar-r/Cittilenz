package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.GeocodingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class IssueEnrichmentService {

    private static final Logger log =
            LoggerFactory.getLogger(IssueEnrichmentService.class);

    private final IssueRepository issueRepository;
    private final GeocodingService geocodingService;

    public IssueEnrichmentService(
            IssueRepository issueRepository,
            GeocodingService geocodingService
    ) {
        this.issueRepository = issueRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * Runs AFTER issue is committed.
     * Updates only address fields.
     */
    @Async
    @Transactional
    public void enrichIssueAddress(Integer issueId) {

        try {

            Optional<Issue> optional = issueRepository.findById(issueId);

            if (optional.isEmpty()) {
                log.warn("Issue {} not found during enrichment", issueId);
                return;
            }

            Issue issue = optional.get();

            // Skip if already enriched
            if (issue.getCity() != null && issue.getPincode() != null) {
                log.info("Issue {} already enriched, skipping", issueId);
                return;
            }

            Map<String, String> address =
                    geocodingService.reverseGeocode(
                            issue.getLatitude(),
                            issue.getLongitude()
                    );

            if (address == null || address.isEmpty()) {
                log.warn("Geocoding returned empty result for issue {}", issueId);
                return;
            }

            if (address.get("street") != null)
                issue.setStreet(address.get("street"));

            if (address.get("area") != null)
                issue.setArea(address.get("area"));

            if (address.get("locality") != null)
                issue.setLocality(address.get("locality"));

            if (address.get("city") != null)
                issue.setCity(address.get("city"));

            if (address.get("pincode") != null)
                issue.setPincode(address.get("pincode"));

            if (address.get("state") != null)
                issue.setState(address.get("state"));

            if (address.get("country") != null)
                issue.setCountry(address.get("country"));

            issueRepository.save(issue);

            log.info("Async enrichment completed for issue {}", issueId);

        } catch (Exception ex) {
            log.error("Async enrichment failed for issue {}", issueId, ex);
        }
    }
}
