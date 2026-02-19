package com.civic_reporting.cittilenz.dto.response;

public class AdminDashboardResponse {

    private long totalCitizens;
    private long totalOfficials;
    private long totalWardSuperiors;
    private long totalIssues;
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

    
}
