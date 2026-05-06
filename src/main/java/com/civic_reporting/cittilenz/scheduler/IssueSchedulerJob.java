package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.IssueSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IssueSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(IssueSchedulerJob.class);

    private final IssueSchedulerService issueSchedulerService;

    // ✅ Explicit constructor
    public IssueSchedulerJob(IssueSchedulerService issueSchedulerService) {
        this.issueSchedulerService = issueSchedulerService;
    }

    /**
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000, initialDelay = 120000) // 5 minutes
    public void runSubmittedTimeoutJob() {

        log.info("Running scheduler job: SUBMITTED timeout handler");

        try {
            issueSchedulerService.processSubmittedTimeout();
        } catch (Exception ex) {
            log.error("Scheduler execution failed", ex);
        }
    }
}