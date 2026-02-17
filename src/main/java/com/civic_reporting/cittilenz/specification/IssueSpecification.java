package com.civic_reporting.cittilenz.specification;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {

    public static Specification<Issue> hasWard(Integer wardId) {
        return (root, query, cb) ->
                wardId == null ? null :
                        cb.equal(root.get("wardId"), wardId);
    }

    public static Specification<Issue> hasDepartment(Integer departmentId) {
        return (root, query, cb) ->
                departmentId == null ? null :
                        cb.equal(root.get("departmentId"), departmentId);
    }

    public static Specification<Issue> hasReporter(Integer reportedBy) {
        return (root, query, cb) ->
                reportedBy == null ? null :
                        cb.equal(root.get("reportedBy"), reportedBy);
    }

    public static Specification<Issue> hasStatus(IssueStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<Issue> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    // ===============================
    // 🔥 MASTER DYNAMIC FILTER BUILDER
    // ===============================

    
}
