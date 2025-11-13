package com.example.tinder.repository;

import com.example.tinder.entity.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    @Query(value = """
    SELECT *
    FROM user_log u
    WHERE (:userId IS NULL OR u.user_id = :userId)
      AND (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
      AND (:action IS NULL OR u.action = :action)
      AND (:endpoint IS NULL OR u.endpoint LIKE CONCAT('%', :endpoint, '%'))
      AND (:ipAddress IS NULL OR u.ip_address = :ipAddress)
    ORDER BY u.request_time DESC
    """, nativeQuery = true)
    Page<UserLog> findAllByFilter(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("action") String action,
            @Param("endpoint") String endpoint,
            @Param("ipAddress") String ipAddress,
            Pageable pageable
    );



}