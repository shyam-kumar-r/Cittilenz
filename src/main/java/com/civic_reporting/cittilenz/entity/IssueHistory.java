package com.civic_reporting.cittilenz.entity;

import com.civic_reporting.cittilenz.enums.IssueStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_status_history")
public class IssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private IssueStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private IssueStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private Integer changedBy;

    private String remarks;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

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

	public Integer getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(Integer changedBy) {
		this.changedBy = changedBy;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}

	public IssueStatus getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(IssueStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	public IssueStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(IssueStatus newStatus) {
		this.newStatus = newStatus;
	}
	
	

    // getters and setters
    
}
