package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.internal.NotificationData;
import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.repository.IssueRepository;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.NotificationProcessorService;
import com.civic_reporting.cittilenz.service.NotificationRouterService;
import com.civic_reporting.cittilenz.service.TemplateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationProcessorServiceImpl
        implements NotificationProcessorService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationProcessorServiceImpl.class);

    private static final int BATCH_SIZE = 50;

    private static final int MAX_RETRIES = 3;

    private static final String LOGO_URL = "https://raw.githubusercontent.com/shyam-kumar-r/Cittilenz/refs/heads/master/src/main/resources/static/Cittilenz%20Logo.jpg";

    private final NotificationRepository notificationRepository;
    private final NotificationRouterService notificationRouterService;
    private final TemplateService templateService;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;

    public NotificationProcessorServiceImpl(
            NotificationRepository notificationRepository,
            NotificationRouterService notificationRouterService,
            TemplateService templateService,
            UserRepository userRepository,
            IssueRepository issueRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationRouterService = notificationRouterService;
        this.templateService = templateService;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }

    // =========================================================
    // MAIN QUEUE PROCESSOR
    // =========================================================

    @Override
    public void processQueue() {

        List<NotificationData> notifications =
                fetchNotificationDataBatch();

        if (notifications.isEmpty()) {

            log.info("No pending notifications");

            return;
        }

        for (NotificationData data : notifications) {

            Notification notification = data.notification();

            try {

                processNotification(data);

            } catch (Exception ex) {

                log.error(
                        "Notification processing failed | id={} type={}",
                        notification.getId(),
                        notification.getNotificationType(),
                        ex
                );

                handleFailure(notification);
            }
        }
    }

    // =========================================================
    // FETCH EVERYTHING IN ONE SHORT TRANSACTION
    // =========================================================

    @Transactional(readOnly = true)
    public List<NotificationData> fetchNotificationDataBatch() {

        List<Notification> notifications =
                notificationRepository.fetchPendingForProcessing(BATCH_SIZE);

        List<NotificationData> result = new ArrayList<>();

        for (Notification notification : notifications) {

            User user = null;
            Issue issue = null;
            String officialName = "Not Assigned";

            if (notification.getUserId() != null) {

                user = userRepository
                        .findById(notification.getUserId())
                        .orElse(null);
            }

            if (notification.getIssueId() != null) {

                issue = issueRepository
                        .findById(notification.getIssueId())
                        .orElse(null);

                if (issue != null &&
                        issue.getAssignedOfficialId() != null) {

                    officialName = userRepository
                            .findById(issue.getAssignedOfficialId())
                            .map(User::getFullName)
                            .orElse("Official");
                }
            }

            result.add(
                    new NotificationData(
                            notification,
                            user,
                            issue,
                            officialName
                    )
            );
        }

        return result;
    }

    // =========================================================
    // PURE MEMORY PROCESSING (NO DB)
    // =========================================================

    private void processNotification(NotificationData data) {

        Notification notification = data.notification();

        log.info(
                "Processing notification | id={} type={}",
                notification.getId(),
                notification.getNotificationType()
        );

        if ("EMAIL".equalsIgnoreCase(notification.getChannel())) {

            String html = buildHtml(data);

            notification.setMessage(html);
        }

        // NO DB CONNECTION HERE
        notificationRouterService.route(notification);

        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notification.setLastAttemptAt(LocalDateTime.now());

        saveNotification(notification);

        log.info(
                "Notification processed successfully | id={}",
                notification.getId()
        );
    }

    // =========================================================
    // SMALL WRITE TRANSACTION
    // =========================================================

    @Transactional
    public void saveNotification(Notification notification) {

        notificationRepository.save(notification);
    }

    // =========================================================
    // TEMPLATE BUILDER (NO DB CALLS)
    // =========================================================

    private String buildHtml(NotificationData data) {

        Notification notification = data.notification();

        String type = safe(notification.getNotificationType());

        User user = data.user();

        Issue issue = data.issue();

        String role = resolveRole(user);

        Map<String, Object> templateData =
                buildBaseData(user, issue, data.officialName());

        return switch (type) {

            case "ISSUE_ASSIGNED" -> switch (role) {

                case "CITIZEN" ->
                        template("issue-assigned-citizen", templateData);

                case "OFFICIAL" ->
                        template("issue-assigned-official", templateData);

                default ->
                        template("issue-assigned-supervisor", templateData);
            };

            case "ISSUE_IN_PROGRESS" -> switch (role) {

                case "CITIZEN" ->
                        template("issue-inprogress-citizen", templateData);

                default ->
                        template("issue-inprogress-official", templateData);
            };

            case "ISSUE_RESOLVED" -> switch (role) {

                case "CITIZEN" ->
                        template("issue-resolved-citizen", templateData);

                default ->
                        template("issue-resolved-official", templateData);
            };

            case "SLA_SOFT_BREACH" -> switch (role) {

                case "CITIZEN" ->
                        template("sla-soft-breach-citizen", templateData);

                default ->
                        template("sla-soft-breach-official", templateData);
            };

            case "SLA_REASSIGNED" ->
                    template("sla-reassigned-official", templateData);

            case "SLA_SUPERVISOR_ALERT" ->
                    template("sla-supervisor-alert", templateData);

            case "SLA_HARD_ESCALATION" -> switch (role) {

                case "CITIZEN" ->
                        template("sla-hard-escalation-citizen", templateData);

                case "OFFICIAL" ->
                        template("sla-hard-escalation-official", templateData);

                default ->
                        template("sla-hard-escalation-supervisor", templateData);
            };

            case "SLA_HARD_ESCALATION_PROCESSED" -> switch (role) {

                case "CITIZEN" ->
                        template("sla-hard-processed-citizen", templateData);

                case "OFFICIAL" ->
                        template("sla-hard-processed-official", templateData);

                default ->
                        template("sla-hard-processed-supervisor", templateData);
            };

            case "ISSUE_CREATED" ->
                    template("issue-created-email", templateData);

            case "ISSUE_LINKED" ->
                    template("issue-linked-email", templateData);

            case "USER_REGISTERED" ->
                    template("registration-email", templateData);

            case "PASSWORD_CHANGED" ->
                    template("password-changed-email", templateData);

            case "ACCOUNT_DEACTIVATED" ->
                    template("account-deactivated-email", templateData);

            case "ACCOUNT_DELETED" ->
                    template("account-deleted-email", templateData);

            case "ADMIN_USER_CREATED" ->
                    template("admin-user-created-email", templateData);

            case "ADMIN_PASSWORD_RESET" ->
                    template("admin-password-reset-email", templateData);

            case "ADMIN_USER_DELETED" ->
                    template("admin-user-deleted-email", templateData);

            default ->
                    throw new IllegalStateException(
                            "No template mapped for type: " + type
                    );
        };
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Map<String, Object> buildBaseData(
            User user,
            Issue issue,
            String officialName
    ) {

        Map<String, Object> data = new HashMap<>();

        data.put(
                "name",
                user != null ? user.getFullName() : "User"
        );

        data.put(
                "email",
                user != null ? user.getEmail() : ""
        );

        data.put(
                "username",
                user != null ? user.getUsername() : ""
        );

        data.put(
                "role",
                user != null && user.getRole() != null
                        ? user.getRole().name()
                        : ""
        );

        data.put("logoUrl", LOGO_URL);

        if (issue != null) {

            data.put("issueId", issue.getId());

            data.put("issueTitle", issue.getTitle());

            data.put(
                    "status",
                    issue.getStatus() != null
                            ? issue.getStatus().name()
                            : ""
            );

            data.put("ward", issue.getWardName());

            data.put("department", issue.getDepartmentName());

            data.put("officialName", officialName);
        }

        return data;
    }

    private String resolveRole(User user) {

        if (user == null || user.getRole() == null) {
            return "UNKNOWN";
        }

        return user.getRole().name();
    }

    private String template(String name, Map<String, Object> data) {

        return templateService.build(name, data);
    }

    private String safe(String value) {

        return value == null ? "" : value;
    }

    // =========================================================
    // FAILURE HANDLER
    // =========================================================

    private void handleFailure(Notification notification) {

        int retry =
                notification.getRetryCount() == null
                        ? 1
                        : notification.getRetryCount() + 1;

        notification.setRetryCount(retry);

        notification.setLastAttemptAt(LocalDateTime.now());

        if (retry >= MAX_RETRIES) {

            notification.setStatus("FAILED");

        } else {

            notification.setStatus("PENDING");
        }

        saveFailure(notification);
    }

    @Transactional
    public void saveFailure(Notification notification) {

        notificationRepository.save(notification);
    }
}