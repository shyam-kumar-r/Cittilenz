package com.civic_reporting.cittilenz.dto.response;

public class SlaAnalyticsResponse {

    private long totalIssues;
    private long assignedIssues;
    private long inProgressIssues;
    private long resolvedIssues;
    private long escalatedIssues;

    private long softSlaBreaches;
    private long hardSlaBreaches;
    private long supervisorInterventionRequired;

    private double averageAcknowledgementMinutes;
    private double averageResolutionMinutes;

    private double escalationRatePercentage;
    private double reassignmentRatePercentage;
    private double slaCompliancePercentage;
	public long getTotalIssues() {
		return totalIssues;
	}
	public void setTotalIssues(long totalIssues) {
		this.totalIssues = totalIssues;
	}
	public long getAssignedIssues() {
		return assignedIssues;
	}
	public void setAssignedIssues(long assignedIssues) {
		this.assignedIssues = assignedIssues;
	}
	public long getInProgressIssues() {
		return inProgressIssues;
	}
	public void setInProgressIssues(long inProgressIssues) {
		this.inProgressIssues = inProgressIssues;
	}
	public long getResolvedIssues() {
		return resolvedIssues;
	}
	public void setResolvedIssues(long resolvedIssues) {
		this.resolvedIssues = resolvedIssues;
	}
	public long getEscalatedIssues() {
		return escalatedIssues;
	}
	public void setEscalatedIssues(long escalatedIssues) {
		this.escalatedIssues = escalatedIssues;
	}
	public long getSoftSlaBreaches() {
		return softSlaBreaches;
	}
	public void setSoftSlaBreaches(long softSlaBreaches) {
		this.softSlaBreaches = softSlaBreaches;
	}
	public long getHardSlaBreaches() {
		return hardSlaBreaches;
	}
	public void setHardSlaBreaches(long hardSlaBreaches) {
		this.hardSlaBreaches = hardSlaBreaches;
	}
	public long getSupervisorInterventionRequired() {
		return supervisorInterventionRequired;
	}
	public void setSupervisorInterventionRequired(long supervisorInterventionRequired) {
		this.supervisorInterventionRequired = supervisorInterventionRequired;
	}
	public double getAverageAcknowledgementMinutes() {
		return averageAcknowledgementMinutes;
	}
	public void setAverageAcknowledgementMinutes(double averageAcknowledgementMinutes) {
		this.averageAcknowledgementMinutes = averageAcknowledgementMinutes;
	}
	public double getAverageResolutionMinutes() {
		return averageResolutionMinutes;
	}
	public void setAverageResolutionMinutes(double averageResolutionMinutes) {
		this.averageResolutionMinutes = averageResolutionMinutes;
	}
	public double getEscalationRatePercentage() {
		return escalationRatePercentage;
	}
	public void setEscalationRatePercentage(double escalationRatePercentage) {
		this.escalationRatePercentage = escalationRatePercentage;
	}
	public double getReassignmentRatePercentage() {
		return reassignmentRatePercentage;
	}
	public void setReassignmentRatePercentage(double reassignmentRatePercentage) {
		this.reassignmentRatePercentage = reassignmentRatePercentage;
	}
	public double getSlaCompliancePercentage() {
		return slaCompliancePercentage;
	}
	public void setSlaCompliancePercentage(double slaCompliancePercentage) {
		this.slaCompliancePercentage = slaCompliancePercentage;
	}

    
}