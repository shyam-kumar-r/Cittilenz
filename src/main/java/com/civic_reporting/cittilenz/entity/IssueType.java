package com.civic_reporting.cittilenz.entity;

import com.civic_reporting.cittilenz.enums.IssuePriority;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "issue_types")
public class IssueType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority;


    private String description;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /* ===== getters & setters ===== */

    public Integer getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public IssuePriority getPriority() {
		return priority;
	}

	public void setPriority(IssuePriority priority) {
		this.priority = priority;
	}

	
}
