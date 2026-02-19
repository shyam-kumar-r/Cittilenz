package com.civic_reporting.cittilenz.dto.response;

public class OfficialDashboardResponse {

    private long totalAssigned;
    private long totalInProgress;
    private long totalResolved;
    private long totalEscalated;
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
	public long getTotalResolved() {
		return totalResolved;
	}
	public void setTotalResolved(long totalResolved) {
		this.totalResolved = totalResolved;
	}
	public long getTotalEscalated() {
		return totalEscalated;
	}
	public void setTotalEscalated(long totalEscalated) {
		this.totalEscalated = totalEscalated;
	}

    
}
