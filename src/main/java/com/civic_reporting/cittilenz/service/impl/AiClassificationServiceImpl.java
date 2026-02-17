package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.IssueType;
import com.civic_reporting.cittilenz.repository.IssueTypeRepository;
import com.civic_reporting.cittilenz.service.AiClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AiClassificationServiceImpl implements AiClassificationService {

    private static final Logger log =
            LoggerFactory.getLogger(AiClassificationServiceImpl.class);

    private final IssueTypeRepository issueTypeRepository;

    public AiClassificationServiceImpl(IssueTypeRepository issueTypeRepository) {
        this.issueTypeRepository = issueTypeRepository;
    }

    @Override
    public IssueType classifyIssue(MultipartFile image) {

        try {
            // ===== FUTURE PRODUCTION AI CALL =====
            /*
            CompletableFuture<IssueType> aiFuture =
                    callAiMicroserviceAsync(image);

            return aiFuture.get(3, TimeUnit.SECONDS);
            */

            // ===== MANUAL FALLBACK (FOR NOW) =====
            log.info("AI model not active. Using manual fallback classification.");

            return issueTypeRepository.findFirstByActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No active issue types found"));

        } catch (Exception ex) {

            log.error("AI classification failed. Falling back.", ex);

            // Safe fallback
            return issueTypeRepository.findFirstByActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No fallback issue type available"));
        }
    }

    /*
    @Async
    public CompletableFuture<IssueType> callAiMicroserviceAsync(MultipartFile image) {
        // Future AI client call
        return CompletableFuture.completedFuture(null);
    }
    */
}
