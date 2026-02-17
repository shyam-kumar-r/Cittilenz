package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.SlaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SLAScheduler {

    private final SlaService slaService;

    public SLAScheduler(SlaService slaService) {
        this.slaService = slaService;
    }

    /**
     * Runs every 5 minutes.
     * You can tune frequency later.
     */
    @Scheduled(fixedDelay = 300000)
    public void runSlaCheck() {
        slaService.checkAndEscalateBreachedIssues();
    }
}
