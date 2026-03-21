package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Fetch pending notifications with row-level lock.
     * SKIP LOCKED prevents duplicate processing in multi-node systems.
     */
    @Query(
        value = """
        SELECT *
        FROM notifications
        WHERE status = 'PENDING'
        AND is_active = true
        ORDER BY created_at
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    List<Notification> fetchPendingForProcessing(@Param("batchSize") int batchSize);

}