package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class IssueTypeCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private Integer departmentId;

    @Positive
    private Integer slaHours;

    @NotBlank
    private String priority;

    private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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
