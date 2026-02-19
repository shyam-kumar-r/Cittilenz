package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;   // ✅ ADD THIS
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends
        JpaRepository<Issue, Integer>,
        JpaSpecificationExecutor<Issue> {   // ✅ ADD THIS

    Optional<Issue> findById(Integer id);

    List<Issue> findByReportedByOrderByCreatedAtDesc(Integer reportedBy);
    
    Page<Issue> findByReportedByAndActiveTrue(
            Integer reportedBy,
            Pageable pageable
    );


    List<Issue> findByWardId(Integer wardId);

    List<Issue> findByDepartmentId(Integer departmentId);

    List<Issue> findByReportedBy(Integer reportedBy);

    List<Issue> findByStatus(IssueStatus status);

    List<Issue> findByWardIdAndDepartmentId(
            Integer wardId,
            Integer departmentId
    );

    List<Issue> findByWardIdAndDepartmentIdAndStatus(
            Integer wardId,
            Integer departmentId,
            IssueStatus status
    );

    List<Issue> findByWardIdAndStatus(
            Integer wardId,
            IssueStatus status
    );

    // Duplicate detection (spatial proximity)
    @Query("""
            SELECT i FROM Issue i
            WHERE i.wardId = :wardId
            AND i.issueTypeId = :issueTypeId
            AND i.status IN (
                com.civic_reporting.cittilenz.enums.IssueStatus.SUBMITTED,
                com.civic_reporting.cittilenz.enums.IssueStatus.ASSIGNED,
                com.civic_reporting.cittilenz.enums.IssueStatus.IN_PROGRESS,
                com.civic_reporting.cittilenz.enums.IssueStatus.ESCALATED
            )
            AND function('ST_DWithin',
                function('geography', i.location),
                function('geography', :point),
                :radius
            ) = true
        """)
    Optional<Issue> findDuplicateSpatial(
            @Param("wardId") Integer wardId,
            @Param("issueTypeId") Integer issueTypeId,
            @Param("point") Point point,
            @Param("radius") Double radius
    );

    @Query("""
            SELECT i FROM Issue i
            WHERE i.slaDeadline IS NOT NULL
            AND i.slaDeadline < :now
            AND i.status NOT IN (
                com.civic_reporting.cittilenz.enums.IssueStatus.RESOLVED,
                com.civic_reporting.cittilenz.enums.IssueStatus.REJECTED
            )
            AND i.active = true
        """)
    List<Issue> findBreachedIssues(@Param("now") LocalDateTime now);
    
    @Query("""
    	    SELECT i.status, COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    GROUP BY i.status
    	""")
    	List<Object[]> countByStatusAdmin();


    	@Query("""
    	    SELECT i.status, COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.reportedBy = :userId
    	    GROUP BY i.status
    	""")
    	List<Object[]> countByStatusCitizen(@Param("userId") Integer userId);


    	@Query("""
    	    SELECT i.status, COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    GROUP BY i.status
    	""")
    	List<Object[]> countByStatusOfficial(@Param("officialId") Integer officialId);


    	@Query("""
    	    SELECT i.status, COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.wardId = :wardId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.ESCALATED
    	    GROUP BY i.status
    	""")
    	List<Object[]> countEscalatedByWard(@Param("wardId") Integer wardId);


    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.slaDeadline IS NOT NULL
    	    AND i.slaDeadline < CURRENT_TIMESTAMP
    	    AND i.status NOT IN (
    	        com.civic_reporting.cittilenz.enums.IssueStatus.RESOLVED,
    	        com.civic_reporting.cittilenz.enums.IssueStatus.REJECTED
    	    )
    	""")
    	long countSlaBreachedAdmin();


    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    AND i.slaDeadline IS NOT NULL
    	    AND i.slaDeadline < CURRENT_TIMESTAMP
    	    AND i.status NOT IN (
    	        com.civic_reporting.cittilenz.enums.IssueStatus.RESOLVED,
    	        com.civic_reporting.cittilenz.enums.IssueStatus.REJECTED
    	    )
    	""")
    	long countSlaBreachedOfficial(@Param("officialId") Integer officialId);
    	
    	
    	// ======================================================
    	// 📊 DASHBOARD ANALYTICS QUERIES (PRODUCTION SAFE)
    	// ======================================================

    	// ================= ADMIN DASHBOARD =================

    	// Total active issues (all statuses)
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	""")
    	long countTotalIssuesAdmin();


    	// ================= CITIZEN DASHBOARD =================

    	// Total reported issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.reportedBy = :userId
    	""")
    	long countTotalIssuesCitizen(@Param("userId") Integer userId);


    	// Resolved issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.reportedBy = :userId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.RESOLVED
    	""")
    	long countResolvedIssuesCitizen(@Param("userId") Integer userId);


    	// Assigned + In Progress issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.reportedBy = :userId
    	    AND i.status IN (
    	        com.civic_reporting.cittilenz.enums.IssueStatus.ASSIGNED,
    	        com.civic_reporting.cittilenz.enums.IssueStatus.IN_PROGRESS
    	    )
    	""")
    	long countActiveWorkIssuesCitizen(@Param("userId") Integer userId);


    	// Escalated issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.reportedBy = :userId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.ESCALATED
    	""")
    	long countEscalatedIssuesCitizen(@Param("userId") Integer userId);



    	// ================= OFFICIAL DASHBOARD =================

    	// Assigned issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.ASSIGNED
    	""")
    	long countAssignedIssuesOfficial(@Param("officialId") Integer officialId);


    	// In Progress issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.IN_PROGRESS
    	""")
    	long countInProgressIssuesOfficial(@Param("officialId") Integer officialId);


    	// Resolved issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.RESOLVED
    	""")
    	long countResolvedIssuesOfficial(@Param("officialId") Integer officialId);


    	// Escalated issues
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.assignedOfficialId = :officialId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.ESCALATED
    	""")
    	long countEscalatedIssuesOfficial(@Param("officialId") Integer officialId);



    	// ================= SUPERIOR DASHBOARD =================

    	// Escalated issues in ward
    	@Query("""
    	    SELECT COUNT(i)
    	    FROM Issue i
    	    WHERE i.active = true
    	    AND i.wardId = :wardId
    	    AND i.status = com.civic_reporting.cittilenz.enums.IssueStatus.ESCALATED
    	""")
    	long countEscalatedIssuesWard(@Param("wardId") Integer wardId);


}
