package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.IssueSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueSchedulerServiceImpl implements IssueSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(IssueSchedulerServiceImpl.class);

    private final IssueRepository issueRepository;

    // ✅ Explicit constructor
    public IssueSchedulerServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    @Transactional
    public void processSubmittedTimeout() {

        log.info("Scheduler started: processing SUBMITTED → UNASSIGNED");

        int updatedCount = issueRepository.markSubmittedAsUnassigned();

        if (updatedCount > 0) {
            log.info("Scheduler success: {} issues updated", updatedCount);
        } else {
            log.info("Scheduler: no issues to update");
        }
    }
}