package com.civic_reporting.cittilenz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_images")
public class IssueImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_type", nullable = false)
    private String imageType; // Must match DB check constraint

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "is_active")
    private boolean active = true;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIssueId() {
		return issueId;
	}

	public void setIssueId(Integer issueId) {
		this.issueId = issueId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public Integer getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(Integer uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

    // getters and setters
    
}
