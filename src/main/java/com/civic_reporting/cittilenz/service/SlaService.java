package com.civic_reporting.cittilenz.service;

import org.springframework.scheduling.annotation.Async;

public interface SlaService {

	void processSlaBreaches();
	
	void processReassignedIssues();
}