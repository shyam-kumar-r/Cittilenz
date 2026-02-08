package com.civic_reporting.cittilenz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // Required for SLA escalation, scheduled checks
@EnableAsync        // Required for AI calls, notifications, email, websocket triggers
public class CittilenzApplication {

    public static void main(String[] args) {
        SpringApplication.run(CittilenzApplication.class, args);
    }

}
