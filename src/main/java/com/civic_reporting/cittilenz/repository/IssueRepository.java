package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.dto.projection.SlaAggregateProjection;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Central Issue Repository.
 *
 * RULE:
 * - No business logic here.
 * - No role validation here.
 * - No transition decisions here.
 * - Only persistence queries.
 */
public interface IssueRepository extends
        JpaRepository<Issue, Integer>,
        JpaSpecificationExecutor<Issue> {

    // ============================================================
    // BASIC FETCH METHODS (UNCHANGED)
    // ============================================================

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

    // ============================================================
    // 🔐 TRANSITION-SAFE FETCH METHODS (NEW)
    // ============================================================

    /**
     * Secure fetch:
     * Used by Official transitions.
     *
     * Prevents modifying issue not assigned to that official.
     */
    Optional<Issue> findByIdAndAssignedOfficialId(
            Integer issueId,
            Integer assignedOfficialId
    );

    /**
     * Fetch escalated issues for specific ward.
     * Used by Ward Superior reassignment.
     */
    List<Issue> findByWardIdAndStatusAndActiveTrue(
            Integer wardId,
            IssueStatus status
    );

    // ============================================================
    // DUPLICATE DETECTION (UNCHANGED)
    // ============================================================

    @Query("""
            SELECT i FROM Issue i
            WHERE i.wardId = :wardId
            AND i.issueTypeId = :issueTypeId
            AND i.status IN ('SUBMITTED','ASSIGNED','IN_PROGRESS','ESCALATED')
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

    // ============================================================
    // SLA BREACH DETECTION (EXISTING - LEFT UNTOUCHED)
    // ============================================================

    // ============================================================
    // SLA COUNTS (UNCHANGED)
    // ============================================================

    @Query("""
    		SELECT COUNT(i)
    		FROM Issue i
    		WHERE i.active = true
    		AND (i.softSlaBreached = true OR i.hardSlaBreached = true)
    		AND i.status <> 'RESOLVED'
    		""")
    		long countSlaBreachedAdmin();


    @Query("""
    		SELECT COUNT(i)
    		FROM Issue i
    		WHERE i.active = true
    		AND i.assignedOfficialId = :officialId
    		AND (i.softSlaBreached = true OR i.hardSlaBreached = true)
    		AND i.status <> 'RESOLVED'
    		""")
    		long countSlaBreachedOfficial(@Param("officialId") Integer officialId);



    // ============================================================
    // DASHBOARD ANALYTICS (UNCHANGED)
    // ============================================================

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
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.wardId = :wardId
            AND i.status = 'ESCALATED'
        """)
    long countEscalatedIssuesWard(@Param("wardId") Integer wardId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
        """)
    long countTotalIssuesAdmin();

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.reportedBy = :userId
        """)
    long countTotalIssuesCitizen(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.reportedBy = :userId
            AND i.status = 'RESOLVED'
        """)
    long countResolvedIssuesCitizen(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.reportedBy = :userId
            AND i.status IN ('ASSIGNED','IN_PROGRESS')
        """)
    long countActiveWorkIssuesCitizen(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.reportedBy = :userId
            AND i.status = 'ESCALATED'
        """)
    long countEscalatedIssuesCitizen(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.assignedOfficialId = :officialId
            AND i.status = 'ASSIGNED'
        """)
    long countAssignedIssuesOfficial(@Param("officialId") Integer officialId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.assignedOfficialId = :officialId
            AND i.status = 'IN_PROGRESS'
        """)
    long countInProgressIssuesOfficial(@Param("officialId") Integer officialId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.assignedOfficialId = :officialId
            AND i.status = 'RESOLVED'
        """)
    long countResolvedIssuesOfficial(@Param("officialId") Integer officialId);

    @Query("""
            SELECT COUNT(i)
            FROM Issue i
            WHERE i.active = true
            AND i.assignedOfficialId = :officialId
            AND i.status = 'ESCALATED'
        """)
    long countEscalatedIssuesOfficial(@Param("officialId") Integer officialId);
    
    @Query(value = "SELECT process_all_sla_breaches()", nativeQuery = true)
    void callProcessAllSlaBreaches();
    
    @Query("SELECT COUNT(i) FROM Issue i")
    long countAllIssues();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.status = 'ASSIGNED'")
    long countAssignedIssues();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.status = 'IN_PROGRESS'")
    long countInProgressIssues();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.status = 'RESOLVED'")
    long countResolvedIssues();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.status = 'ESCALATED'")
    long countEscalatedIssues();
    
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.softSlaBreached = true")
    long countSoftBreaches();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.hardSlaBreached = true")
    long countHardBreaches();

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.requiresSupervisorIntervention = true")
    long countSupervisorInterventions();
    
    @Query(value = """
    		SELECT AVG(EXTRACT(EPOCH FROM (started_at - assigned_at)) / 60)
    		FROM issues
    		WHERE started_at IS NOT NULL
    		AND assigned_at IS NOT NULL
    		""", nativeQuery = true)
    		Double averageAcknowledgementMinutes();
    
    @Query(value = """
    		SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - started_at)) / 60)
    		FROM issues
    		WHERE resolved_at IS NOT NULL
    		AND started_at IS NOT NULL
    		""", nativeQuery = true)
    		Double averageResolutionMinutes();
    
    @Query("""
    		SELECT COUNT(i) FROM Issue i
    		WHERE i.escalationCount > 0
    		""")
    		long countEscalatedAtLeastOnce();
    
    @Query("""
    		SELECT COUNT(i) FROM Issue i
    		WHERE i.reassignmentCount > 0
    		""")
    		long countReassignedAtLeastOnce();
    
    @Query(value = """
    		SELECT
    		COUNT(i) as total,

    		SUM(CASE WHEN i.status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned,
    		SUM(CASE WHEN i.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress,
    		SUM(CASE WHEN i.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved,
    		SUM(CASE WHEN i.status = 'ESCALATED' THEN 1 ELSE 0 END) as escalated,

    		SUM(CASE WHEN i.softSlaBreached = true THEN 1 ELSE 0 END) as softBreached,
    		SUM(CASE WHEN i.hardSlaBreached = true THEN 1 ELSE 0 END) as hardBreached,
    		SUM(CASE WHEN i.requiresSupervisorIntervention = true THEN 1 ELSE 0 END) as supervisorRequired,

    		AVG(CASE WHEN i.startedAt IS NOT NULL AND i.assignedAt IS NOT NULL
    		THEN EXTRACT(EPOCH FROM (i.startedAt - i.assignedAt))/60 ELSE NULL END) as avgAckMinutes,

    		AVG(CASE WHEN i.resolvedAt IS NOT NULL AND i.startedAt IS NOT NULL
    		THEN EXTRACT(EPOCH FROM (i.resolvedAt - i.startedAt))/60 ELSE NULL END) as avgResolutionMinutes,

    		SUM(CASE WHEN i.escalationCount > 0 THEN 1 ELSE 0 END) as escalatedOnce,
    		SUM(CASE WHEN i.reassignmentCount > 0 THEN 1 ELSE 0 END) as reassignedOnce

    		FROM Issue i
    		WHERE
    		(:wardId IS NULL OR i.wardId = :wardId)
    		AND (:departmentId IS NULL OR i.departmentId = :departmentId)
    		AND (:fromDate IS NULL OR i.createdAt >= :fromDate)
    		AND (:toDate IS NULL OR i.createdAt <= :toDate)
    		""", nativeQuery = true)
    		SlaAggregateProjection getAggregatedAnalytics(
    		        Integer wardId,
    		        Integer departmentId,
    		        LocalDateTime fromDate,
    		        LocalDateTime toDate
    		);
}