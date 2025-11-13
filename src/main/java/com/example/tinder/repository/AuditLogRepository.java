package com.example.tinder.repository;

import com.example.tinder.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query(value = """
    SELECT *
    FROM audit_log att
    WHERE (:action IS NULL OR att.action = :action)
      AND (:username IS NULL OR att.username = :username)
      AND (:userId IS NULL OR att.user_id = :userId)
      AND (:role IS NULL OR :role = ANY(att.roles))
      AND (:ipAddress IS NULL OR att.ip_address = :ipAddress)
      AND (:endPoint IS NULL OR att.endpoint = :endPoint)
      AND (:sourceType IS NULL OR att.source_type = :sourceType)
      AND (:entityName IS NULL OR att.entity_name = :entityName)
      AND (:entityId IS NULL OR att.entity_id = :entityId)
      AND (att.time BETWEEN COALESCE(:timeAfter, att.time) AND COALESCE(:timeBefore, att.time))
    ORDER BY att.time DESC
    """, nativeQuery = true)
    Page<AuditLog> findAllAuditLogs(
            @Param("action") String action,
            @Param("username") String username,
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("ipAddress") String ipAddress,
            @Param("endPoint") String endPoint,
            @Param("sourceType") String sourceType,
            @Param("entityName") String entityName,
            @Param("entityId") Long entityId,
            @Param("timeAfter") LocalDateTime timeAfter,
            @Param("timeBefore") LocalDateTime timeBefore,
            Pageable pageable
    );


}