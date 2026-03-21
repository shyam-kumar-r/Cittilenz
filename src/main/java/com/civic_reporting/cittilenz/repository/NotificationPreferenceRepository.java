package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.NotificationPreference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference,Integer> {

    Optional<NotificationPreference> findByUserIdAndNotificationType(
            Integer userId,
            String notificationType
    );

}