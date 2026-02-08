package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueTypeRepository extends JpaRepository<IssueType, Integer> {

    @Query("""
        select it from IssueType it
        join fetch it.department
        where it.active = true
    """)
    List<IssueType> findActiveWithDepartment();

    @Query("""
        select it from IssueType it
        join fetch it.department
        where it.active = true
          and it.department.id = :departmentId
    """)
    List<IssueType> findActiveByDepartmentWithDepartment(Integer departmentId);

    @Query("""
        select it from IssueType it
        join fetch it.department
        where it.id = :id
    """)
    Optional<IssueType> findByIdWithDepartment(Integer id);

    boolean existsByNameIgnoreCase(String name);
}
