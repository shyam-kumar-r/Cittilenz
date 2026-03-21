package com.civic_reporting.cittilenz.service;

import org.springframework.web.multipart.MultipartFile;

import com.civic_reporting.cittilenz.entity.Issue;

public interface IssueLifecycleService {

	Issue startWork(Integer issueId, Integer officialId, Long version);

	Issue resolveIssue(
	        Integer issueId,
	        Integer officialId,
	        Long version,
	        MultipartFile image
	);

	Issue reassignEscalatedIssue(Integer issueId, Integer superiorId, Long version);

	Issue supervisorReassignSoftBreached(Integer issueId, Integer superiorId, Long version);

	Issue supervisorClearIntervention(Integer issueId, Integer superiorId, Long version, String remarks);
}