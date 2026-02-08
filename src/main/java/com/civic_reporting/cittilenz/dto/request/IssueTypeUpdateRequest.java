package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.Positive;

public class IssueTypeUpdateRequest {

    private Integer departmentId;

    @Positive
    private Integer slaHours;

    private String priority;

    private String description;

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getSlaHours() {
		return slaHours;
	}

	public void setSlaHours(Integer slaHours) {
		this.slaHours = slaHours;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
    
    
}
