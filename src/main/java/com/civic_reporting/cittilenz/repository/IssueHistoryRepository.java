package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Integer> {

    List<IssueHistory> findByIssueIdOrderByChangedAtAsc(Integer issueId);
}
