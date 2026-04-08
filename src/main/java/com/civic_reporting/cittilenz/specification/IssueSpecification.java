package com.civic_reporting.cittilenz.specification;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {

    private IssueSpecification() {}

    public static Specification<Issue> filter(
            Integer wardId,
            Integer departmentId,
            Integer reportedBy,
            IssueStatus status
    ) {

        return Specification
                .where(isActive())
                .and(hasWard(wardId))
                .and(hasDepartment(departmentId))
                .and(hasReporter(reportedBy))
                .and(hasStatus(status));
    }

    public static Specification<Issue> hasWard(Integer wardId) {
        return (root, query, cb) -> {
            if (wardId == null) return cb.conjunction();
            if (wardId <= 0) {
                throw new IllegalArgumentException("Invalid wardId");
            }
            return cb.equal(root.get("wardId"), wardId);
        };
    }

    public static Specification<Issue> hasDepartment(Integer departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            if (departmentId <= 0) {
                throw new IllegalArgumentException("Invalid departmentId");
            }
            return cb.equal(root.get("departmentId"), departmentId);
        };
    }

    public static Specification<Issue> hasReporter(Integer reportedBy) {
        return (root, query, cb) -> {
            if (reportedBy == null) return cb.conjunction();
            if (reportedBy <= 0) {
                throw new IllegalArgumentException("Invalid reporterId");
            }
            return cb.equal(root.get("reportedBy"), reportedBy);
        };
    }

    public static Specification<Issue> hasStatus(IssueStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Issue> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }
}