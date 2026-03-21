package com.civic_reporting.cittilenz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@SpringBootApplication
@EnableScheduling   // Required for SLA escalation, scheduled checks
@EnableAsync        // Required for AI calls, notifications, email, websocket triggers
@EnableCaching
@EnableSchedulerLock(defaultLockAtMostFor = "PT2M")
@EnableMethodSecurity
public class CittilenzApplication {

    public static void main(String[] args) {
        SpringApplication.run(CittilenzApplication.class, args);
    }

}
