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

    @Async
    @Transactional
    public void enrichIssueAddress(Integer issueId) {

        try {

            Optional<Issue> optional = issueRepository.findById(issueId);

            if (optional.isEmpty()) {
                log.warn("Enrichment skipped: issue not found | issueId={}", issueId);
                return;
            }

            Issue issue = optional.get();

            if (issue.getLatitude() == null || issue.getLongitude() == null) {
                log.warn("Enrichment skipped: missing coordinates | issueId={}", issueId);
                return;
            }

            if (issue.getCity() != null && issue.getPincode() != null) {
                log.info("Enrichment skipped: already enriched | issueId={}", issueId);
                return;
            }

            Map<String, String> address =
                    geocodingService.reverseGeocode(
                            issue.getLatitude(),
                            issue.getLongitude()
                    );

            if (address == null || address.isEmpty()) {
                log.warn("Geocoding returned empty | issueId={}", issueId);
                return;
            }

            issue.setStreet(address.get("street"));
            issue.setArea(address.get("area"));
            issue.setLocality(address.get("locality"));
            issue.setCity(address.get("city"));
            issue.setPincode(address.get("pincode"));
            issue.setState(address.get("state"));
            issue.setCountry(address.get("country"));

            issueRepository.save(issue);

            log.info("Enrichment success | issueId={}", issueId);

        } catch (Exception ex) {
            log.error("Enrichment failed | issueId={}", issueId, ex);
        }
    }
}