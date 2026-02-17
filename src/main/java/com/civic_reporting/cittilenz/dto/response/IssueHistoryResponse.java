package com.civic_reporting.cittilenz.dto.response;

import com.civic_reporting.cittilenz.enums.IssueStatus;
import java.time.LocalDateTime;

public class IssueHistoryResponse {

    private IssueStatus oldStatus;
    private IssueStatus newStatus;
    private String changedByName;
    private LocalDateTime changedAt;
    private String remarks;

    public IssueStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(IssueStatus oldStatus) { this.oldStatus = oldStatus; }

    public IssueStatus getNewStatus() { return newStatus; }
    public void setNewStatus(IssueStatus newStatus) { this.newStatus = newStatus; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
