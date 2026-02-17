package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.request.IssueCreateRequest;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.enums.UserRole;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IssueService {

	Issue createIssue(
		    IssueCreateRequest request,
		    MultipartFile image,
		    Integer reporterId
		);

    Issue getIssueById(Integer issueId,
                       Integer viewerId,
                       UserRole role);

    List<Issue> getIssuesByReporter(Integer reporterId);
    
   
    Issue linkDuplicate(Integer issueId, Integer reporterId);

}
