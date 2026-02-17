package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;   // ✅ ADD THIS
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends
        JpaRepository<Issue, Integer>,
        JpaSpecificationExecutor<Issue> {   // ✅ ADD THIS

    Optional<Issue> findById(Integer id);

    List<Issue> findByReportedByOrderByCreatedAtDesc(Integer reportedBy);

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
}
