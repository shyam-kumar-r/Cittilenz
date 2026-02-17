package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.IssueImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueImageRepository extends JpaRepository<IssueImage, Integer> {

    List<IssueImage> findByIssueId(Integer issueId);
}
