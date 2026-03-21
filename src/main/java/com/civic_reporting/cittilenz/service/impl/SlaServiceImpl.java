package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.service.SlaService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlaServiceImpl implements SlaService {

    private final IssueRepository issueRepository;
    private final JdbcTemplate jdbcTemplate;

    public SlaServiceImpl(IssueRepository issueRepository, JdbcTemplate jdbcTemplate) {
        this.issueRepository = issueRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

	@Override
	@Transactional
	public void processSlaBreaches() {
		issueRepository.callProcessAllSlaBreaches();
	}

	@Override
	public void processReassignedIssues() {
	    jdbcTemplate.execute("SELECT process_reassigned_to_assigned()");
	}
}