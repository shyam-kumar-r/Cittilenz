package com.civic_reporting.cittilenz.dto.response;

import com.civic_reporting.cittilenz.enums.IssueStatus;

import java.util.Map;

public class DashboardAnalyticsResponse {

    

    private Map<IssueStatus, Long> statusCounts;

    private long slaBreachedCount;
    
 // Admin
    private long totalCitizens;
    private long totalOfficials;
    private long totalWardSuperiors;
    private long totalIssues;

    // Citizen
    private long totalReported;
    private long totalResolved;
    private long totalAssignedOrInProgress;
    private long totalEscalated;

    // Official
    private long totalAssigned;
    private long totalInProgress;
    
    // getters and setters

    public Map<IssueStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<IssueStatus, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public long getSlaBreachedCount() {
        return slaBreachedCount;
    }

    public void setSlaBreachedCount(long slaBreachedCount) {
        this.slaBreachedCount = slaBreachedCount;
    }

	public long getTotalCitizens() {
		return totalCitizens;
	}

	public void setTotalCitizens(long totalCitizens) {
		this.totalCitizens = totalCitizens;
	}

	public long getTotalOfficials() {
		return totalOfficials;
	}

	public void setTotalOfficials(long totalOfficials) {
		this.totalOfficials = totalOfficials;
	}

	public long getTotalWardSuperiors() {
		return totalWardSuperiors;
	}

	public void setTotalWardSuperiors(long totalWardSuperiors) {
		this.totalWardSuperiors = totalWardSuperiors;
	}

	public long getTotalIssues() {
		return totalIssues;
	}

	public void setTotalIssues(long totalIssues) {
		this.totalIssues = totalIssues;
	}

	public long getTotalReported() {
		return totalReported;
	}

	public void setTotalReported(long totalReported) {
		this.totalReported = totalReported;
	}

	public long getTotalResolved() {
		return totalResolved;
	}

	public void setTotalResolved(long totalResolved) {
		this.totalResolved = totalResolved;
	}

	public long getTotalAssignedOrInProgress() {
		return totalAssignedOrInProgress;
	}

	public void setTotalAssignedOrInProgress(long totalAssignedOrInProgress) {
		this.totalAssignedOrInProgress = totalAssignedOrInProgress;
	}

	public long getTotalEscalated() {
		return totalEscalated;
	}

	public void setTotalEscalated(long totalEscalated) {
		this.totalEscalated = totalEscalated;
	}

	public long getTotalAssigned() {
		return totalAssigned;
	}

	public void setTotalAssigned(long totalAssigned) {
		this.totalAssigned = totalAssigned;
	}

	public long getTotalInProgress() {
		return totalInProgress;
	}

	public void setTotalInProgress(long totalInProgress) {
		this.totalInProgress = totalInProgress;
	}
    
    
	
}
