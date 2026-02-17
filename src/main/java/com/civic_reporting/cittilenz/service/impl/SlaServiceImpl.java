package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.SlaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaServiceImpl implements SlaService {
	
	private static final Logger log =
	        LoggerFactory.getLogger(SlaServiceImpl.class);


    private final IssueRepository issueRepository;

    public SlaServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    /**
     * Detects SLA breached issues and escalates them.
     * This method is triggered by scheduler.
     */
    @Override
    @Async
    public void checkAndEscalateBreachedIssues() {

    	List<Issue> breached = issueRepository.findBreachedIssues(LocalDateTime.now());


        for (Issue issue : breached) {

            // Prevent double escalation
            if (issue.getStatus() == IssueStatus.ESCALATED) {
                continue;
            }

            issue.setStatus(IssueStatus.ESCALATED);
            issue.setAssignedAt(LocalDateTime.now());

            issueRepository.save(issue);
        }
    }
}
