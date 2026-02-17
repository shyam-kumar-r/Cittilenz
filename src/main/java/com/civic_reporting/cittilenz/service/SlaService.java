package com.civic_reporting.cittilenz.service;

import org.springframework.scheduling.annotation.Async;

public interface SlaService {
	
	@Async
    void checkAndEscalateBreachedIssues();
}
