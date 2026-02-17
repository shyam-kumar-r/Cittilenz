package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.IssueType;
import org.springframework.web.multipart.MultipartFile;

public interface AiClassificationService {

    IssueType classifyIssue(MultipartFile image);
}
