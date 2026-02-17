package com.civic_reporting.cittilenz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_reporters",
       uniqueConstraints = @UniqueConstraint(columnNames = {"issue_id", "user_id"}))
public class IssueReporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIssueId() {
		return issueId;
	}

	public void setIssueId(Integer issueId) {
		this.issueId = issueId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public LocalDateTime getReportedAt() {
		return reportedAt;
	}

	public void setReportedAt(LocalDateTime reportedAt) {
		this.reportedAt = reportedAt;
	}

    // getters and setters
    
}
