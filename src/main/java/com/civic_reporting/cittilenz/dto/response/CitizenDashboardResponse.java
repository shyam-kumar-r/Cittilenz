package com.civic_reporting.cittilenz.dto.response;

public class CitizenDashboardResponse {

    private long totalReported;
    private long totalResolved;
    private long totalAssignedOrInProgress;
    private long totalEscalated;
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

    
}
