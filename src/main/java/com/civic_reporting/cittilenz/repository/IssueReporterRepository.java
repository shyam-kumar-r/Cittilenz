package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.IssueReporter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueReporterRepository extends JpaRepository<IssueReporter, Integer> {

    Optional<IssueReporter> findByIssueIdAndUserId(Integer issueId, Integer userId);
}
