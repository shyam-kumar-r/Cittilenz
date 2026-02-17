package com.civic_reporting.cittilenz.entity;

import com.civic_reporting.cittilenz.enums.IssuePriority;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "issue_type_id")
    private Integer issueTypeId;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "ward_id", nullable = false)
    private Integer wardId;

    @Column(name = "reported_by", nullable = false)
    private Integer reportedBy;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "resolved_image_url")
    private String resolvedImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority;

    @Column(name = "assigned_official_id")
    private Integer assignedOfficialId;

    @Column(name = "report_count")
    private Integer reportCount = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    private String street;
    private String area;
    private String locality;
    private String city;
    private String pincode;
    private String state;
    private String country;
    private String landmark;

    @Column(name = "ward_name", nullable = false)
    private String wardName;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "reported_by_name", nullable = false)
    private String reportedByName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getIssueTypeId() {
		return issueTypeId;
	}

	public void setIssueTypeId(Integer issueTypeId) {
		this.issueTypeId = issueTypeId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getWardId() {
		return wardId;
	}

	public void setWardId(Integer wardId) {
		this.wardId = wardId;
	}

	public Integer getReportedBy() {
		return reportedBy;
	}

	public void setReportedBy(Integer reportedBy) {
		this.reportedBy = reportedBy;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getResolvedImageUrl() {
		return resolvedImageUrl;
	}

	public void setResolvedImageUrl(String resolvedImageUrl) {
		this.resolvedImageUrl = resolvedImageUrl;
	}

	public Integer getAssignedOfficialId() {
		return assignedOfficialId;
	}

	public void setAssignedOfficialId(Integer assignedOfficialId) {
		this.assignedOfficialId = assignedOfficialId;
	}

	public Integer getReportCount() {
		return reportCount;
	}

	public void setReportCount(Integer reportCount) {
		this.reportCount = reportCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public LocalDateTime getSlaDeadline() {
		return slaDeadline;
	}

	public void setSlaDeadline(LocalDateTime slaDeadline) {
		this.slaDeadline = slaDeadline;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getWardName() {
		return wardName;
	}

	public void setWardName(String wardName) {
		this.wardName = wardName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getReportedByName() {
		return reportedByName;
	}

	public void setReportedByName(String reportedByName) {
		this.reportedByName = reportedByName;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
	}

	public IssuePriority getPriority() {
		return priority;
	}

	public void setPriority(IssuePriority priority) {
		this.priority = priority;
	}

    // getters and setters
    
}
