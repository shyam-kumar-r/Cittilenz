package com.civic_reporting.cittilenz.dto.response;

import com.civic_reporting.cittilenz.enums.IssuePriority;
import com.civic_reporting.cittilenz.enums.IssueStatus;

import java.time.LocalDateTime;
import java.util.List;

public class IssueResponse {

    // =========================
    // Core Details
    // =========================
    private String title;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String issueTypeName;

    // =========================
    // Address
    // =========================
    private String street;
    private String area;
    private String locality;
    private String city;
    private String pincode;

    // =========================
    // Ward
    // =========================
    private Integer wardId;
    private String wardName;

    // =========================
    // Department
    // =========================
    private Integer departmentId;
    private String departmentName;

    // =========================
    // Reporter
    // =========================
    private String reportedByName;

    // =========================
    // Official Assignment
    // =========================
    private String assignedOfficialName;
    private String assignedOfficialMobile;
    private String assignedOfficialEmail;

    // =========================
    // Ward Superior
    // =========================
    private String wardSuperiorName;
    private String wardSuperiorMobile;
    private String wardSuperiorEmail;

    // =========================
    // Lifecycle
    // =========================
    private IssueStatus status;
    private IssuePriority priority;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime slaDeadline;
    private boolean active;

    // =========================
    // Timeline
    // =========================
    private List<IssueHistoryResponse> history;

    // =========================
    // Getters & Setters
    // =========================

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getIssueTypeName() { return issueTypeName; }
    public void setIssueTypeName(String issueTypeName) { this.issueTypeName = issueTypeName; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Integer getWardId() { return wardId; }
    public void setWardId(Integer wardId) { this.wardId = wardId; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }

    public String getAssignedOfficialName() { return assignedOfficialName; }
    public void setAssignedOfficialName(String assignedOfficialName) { this.assignedOfficialName = assignedOfficialName; }

    public String getAssignedOfficialMobile() { return assignedOfficialMobile; }
    public void setAssignedOfficialMobile(String assignedOfficialMobile) { this.assignedOfficialMobile = assignedOfficialMobile; }

    public String getAssignedOfficialEmail() { return assignedOfficialEmail; }
    public void setAssignedOfficialEmail(String assignedOfficialEmail) { this.assignedOfficialEmail = assignedOfficialEmail; }

    public String getWardSuperiorName() { return wardSuperiorName; }
    public void setWardSuperiorName(String wardSuperiorName) { this.wardSuperiorName = wardSuperiorName; }

    public String getWardSuperiorMobile() { return wardSuperiorMobile; }
    public void setWardSuperiorMobile(String wardSuperiorMobile) { this.wardSuperiorMobile = wardSuperiorMobile; }

    public String getWardSuperiorEmail() { return wardSuperiorEmail; }
    public void setWardSuperiorEmail(String wardSuperiorEmail) { this.wardSuperiorEmail = wardSuperiorEmail; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }

    public Integer getReportCount() { return reportCount; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<IssueHistoryResponse> getHistory() { return history; }
    public void setHistory(List<IssueHistoryResponse> history) { this.history = history; }
}
