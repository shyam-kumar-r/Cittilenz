package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.service.EmailService;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "your-email@gmail.com"; // 🔥 CHANGE

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            // ALWAYS HTML (your system is HTML-based now)
            helper.setText(body, true);

            mailSender.send(message);

            log.info("Email sent successfully to={}", to);

        } catch (Exception e) {

            log.error("Email failed to send to={} subject={}", to, subject, e);

            throw new RuntimeException(
                    "Email sending failed for: " + to,
                    e
            );
        }
    }
}