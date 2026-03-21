package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Integer> {

    List<IssueHistory> findByIssueIdOrderByChangedAtAsc(Integer issueId);
    
    @Query(value = """
    	    SELECT ih.changed_by
    	    FROM issue_status_history ih
    	    WHERE ih.issue_id = :issueId
    	    AND ih.changed_by IN (
    	        SELECT id FROM users WHERE role = 'OFFICIAL'
    	    )
    	    ORDER BY ih.changed_at DESC
    	    LIMIT :limit
    	""", nativeQuery = true)
    	List<Integer> findTopOfficialsForIssue(Integer issueId, int limit);
}
