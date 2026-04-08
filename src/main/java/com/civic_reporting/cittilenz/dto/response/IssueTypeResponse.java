package com.civic_reporting.cittilenz.dto.response;

public class IssueTypeResponse {

    private Integer id;
    private String name;
    private String displayName;
    private String normalizedName;

    private Integer departmentId;
    private String departmentName;

    private Integer slaHours;
    private String priority;
    private boolean active;
    private String description;

    public IssueTypeResponse() {}

    // ================= GETTERS =================

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    // ================= SETTERS =================

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}