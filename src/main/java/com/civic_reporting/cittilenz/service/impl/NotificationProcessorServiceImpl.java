package com.civic_reporting.cittilenz.service.impl;

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
public class NotificationProcessorServiceImpl implements NotificationProcessorService {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessorServiceImpl.class);

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final String LOGO_URL =
            "https://raw.githubusercontent.com/shyam-kumar-r/Cittilenz/master/src/main/resources/static/logo.jpeg";

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

    /**
     * FIX: Removed @Transactional from this method.
     * This allows each repository call to use its own short-lived transaction.
     */
    @Override
    public void processQueue() {
        // READ OPERATION: Acquires connection, reads data, releases immediately
        List<Notification> notifications = fetchPendingNotifications();

        if (notifications.isEmpty()) {
            log.info("No pending notifications to process");
            return;
        }

        // Process each notification individually without holding a connection
        for (Notification notification : notifications) {
            try {
                processNotification(notification);
            } catch (Exception ex) {
                log.error("Notification failed id={} type={}",
                        notification.getId(),
                        notification.getNotificationType(),
                        ex);
                handleFailure(notification);
            }
        }
    }

    /**
     * NEW METHOD: Separated read operation into its own transactional method.
     * This ensures the connection is released immediately after reading.
     */
    @Transactional(readOnly = true)
    private List<Notification> fetchPendingNotifications() {
        return notificationRepository.fetchPendingForProcessing(BATCH_SIZE);
    }

    // =========================================================
    // CORE PROCESSOR
    // =========================================================

    private void processNotification(Notification notification) {
        log.info("Processing notification id={} type={} channel={}",
                notification.getId(),
                notification.getNotificationType(),
                notification.getChannel());

        if ("EMAIL".equalsIgnoreCase(notification.getChannel())) {
            String html = buildHtml(notification);
            notification.setMessage(html);
        }

        // NETWORK I/O: Send email (NO connection held here)
        notificationRouterService.route(notification);

        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notification.setLastAttemptAt(LocalDateTime.now());

        // WRITE OPERATION: Save changes (acquires connection, saves, releases)
        saveNotification(notification);

        log.info("Notification sent successfully id={}", notification.getId());
    }

    /**
     * NEW METHOD: Separated write operation into its own transactional method.
     * This ensures minimal connection hold time.
     */
    @Transactional
    private void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    // =========================================================
    // TEMPLATE ENGINE
    // =========================================================

    private String buildHtml(Notification notification) {
        String type = safe(notification.getNotificationType());

        if (type.isBlank()) {
            throw new IllegalStateException("Notification type is missing for id=" + notification.getId());
        }

        log.info("Building HTML for notification id={} type={}", notification.getId(), type);

        User user = fetchUser(notification.getUserId());
        Issue issue = fetchIssue(notification.getIssueId());

        String role = resolveRole(user);
        Map<String, Object> data = buildBaseData(user, issue);
        log.info("DATA MAP: {}", data);

        String html = switch (type) {
            // ================= ISSUE =================
            case "ISSUE_ASSIGNED" -> switch (role) {
                case "CITIZEN" -> template("issue-assigned-citizen", data);
                case "OFFICIAL" -> template("issue-assigned-official", data);
                default -> template("issue-assigned-supervisor", data);
            };
            case "ISSUE_IN_PROGRESS" -> switch (role) {
                case "CITIZEN" -> template("issue-inprogress-citizen", data);
                case "OFFICIAL" -> template("issue-inprogress-official", data);
                default -> template("issue-inprogress-official", data);
            };
            case "ISSUE_RESOLVED" -> switch (role) {
                case "CITIZEN" -> template("issue-resolved-citizen", data);
                case "OFFICIAL" -> template("issue-resolved-official", data);
                default -> template("issue-resolved-official", data);
            };

            // ================= SLA =================
            case "SLA_SOFT_BREACH" -> switch (role) {
                case "CITIZEN" -> template("sla-soft-breach-citizen", data);
                case "OFFICIAL" -> template("sla-soft-breach-official", data);
                default -> template("sla-soft-breach-citizen", data);
            };
            case "SLA_REASSIGNED" -> template("sla-reassigned-official", data);
            case "SLA_SUPERVISOR_ALERT" -> template("sla-supervisor-alert", data);
            case "SLA_HARD_ESCALATION" -> switch (role) {
                case "CITIZEN" -> template("sla-hard-escalation-citizen", data);
                case "OFFICIAL" -> template("sla-hard-escalation-official", data);
                case "WARD_SUPERIOR" -> template("sla-hard-escalation-supervisor", data);
                default -> template("sla-hard-escalation-citizen", data);
            };
            case "SLA_HARD_ESCALATION_PROCESSED" -> switch (role) {
                case "CITIZEN" -> template("sla-hard-processed-citizen", data);
                case "OFFICIAL" -> template("sla-hard-processed-official", data);
                case "WARD_SUPERIOR" -> template("sla-hard-processed-supervisor", data);
                default -> template("sla-hard-processed-citizen", data);
            };

            // ================= ISSUE CREATION =================
            case "ISSUE_CREATED" -> template("issue-created-email", data);
            case "ISSUE_LINKED" -> template("issue-linked-email", data);

            // ================= USER =================
            case "USER_REGISTERED" -> template("registration-email", data);
            case "PASSWORD_CHANGED" -> template("password-changed-email", data);
            case "ACCOUNT_DEACTIVATED" -> template("account-deactivated-email", data);
            case "ACCOUNT_DELETED" -> template("account-deleted-email", data);
            case "ADMIN_USER_CREATED" -> template("admin-user-created-email", data);
            case "ADMIN_PASSWORD_RESET" -> template("admin-password-reset-email", data);
            case "ADMIN_USER_DELETED" -> template("admin-user-deleted-email", data);

            default -> throw new IllegalStateException("No template mapped for type: " + type);
        };

        log.info("Template resolved successfully for type={}", type);
        return html;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String resolveRole(User user) {
        if (user == null || user.getRole() == null) return "UNKNOWN";
        return user.getRole().name();
    }

    private Map<String, Object> buildBaseData(User user, Issue issue) {
        Map<String, Object> data = new HashMap<>();

        data.put("name", user != null ? user.getFullName() : "User");
        data.put("email", user != null ? user.getEmail() : "");
        data.put("username", user != null ? user.getUsername() : "");
        data.put("role", user != null && user.getRole() != null ? user.getRole().name() : "");
        data.put("logoUrl", LOGO_URL);

        if (issue != null) {
            data.put("issueId", issue.getId());
            data.put("issueTitle", issue.getTitle());
            data.put("status", issue.getStatus() != null ? issue.getStatus().name() : "");
            data.put("ward", issue.getWardName());
            data.put("department", issue.getDepartmentName());
            data.put("officialName", fetchOfficialName(issue));
        }

        return data;
    }

    private String fetchOfficialName(Issue issue) {
        if (issue.getAssignedOfficialId() == null) return "Not Assigned";
        return userRepository.findById(issue.getAssignedOfficialId())
                .map(User::getFullName)
                .orElse("Official");
    }

    private String template(String name, Map<String, Object> data) {
        return templateService.build(name, data);
    }

    private User fetchUser(Integer id) {
        if (id == null) return null;
        return userRepository.findById(id).orElse(null);
    }

    private Issue fetchIssue(Integer id) {
        if (id == null) return null;
        return issueRepository.findById(id).orElse(null);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // =========================================================
    // FAILURE HANDLING
    // =========================================================

    /**
     * Handle notification processing failures with retry logic.
     */
    private void handleFailure(Notification notification) {
        int retry = notification.getRetryCount() == null ? 1 : notification.getRetryCount() + 1;
        notification.setRetryCount(retry);
        notification.setLastAttemptAt(LocalDateTime.now());

        if (retry >= MAX_RETRIES) {
            notification.setStatus("FAILED");
        } else {
            notification.setStatus("PENDING");
        }

        saveNotificationFailure(notification);
    }

    /**
     * NEW METHOD: Separated failure save into its own transactional method.
     */
    @Transactional
    private void saveNotificationFailure(Notification notification) {
        notificationRepository.save(notification);
    }
}