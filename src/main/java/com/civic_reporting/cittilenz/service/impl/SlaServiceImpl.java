package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.SlaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlaServiceImpl implements SlaService {

    private static final Logger log =
            LoggerFactory.getLogger(SlaServiceImpl.class);

    private final IssueRepository issueRepository;
    private final JdbcTemplate jdbcTemplate;

    public SlaServiceImpl(IssueRepository issueRepository, JdbcTemplate jdbcTemplate) {
        this.issueRepository = issueRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void processSlaBreaches() {
        try {
            log.info("SLA processing started");

            issueRepository.callProcessAllSlaBreaches();

            log.info("SLA processing completed");

        } catch (Exception ex) {
            log.error("SLA processing failed", ex);
            throw new IllegalStateException("SLA processing failed");
        }
    }

    @Override
    public void processReassignedIssues() {
        try {
            log.info("Reassignment processing started");

            jdbcTemplate.execute("SELECT process_reassigned_to_assigned()");

            log.info("Reassignment processing completed");

        } catch (Exception ex) {
            log.error("Reassignment processing failed", ex);
            throw new IllegalStateException("Reassignment processing failed");
        }
    }
}