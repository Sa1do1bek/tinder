package com.example.tinder.repository;

import com.example.tinder.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "select * from notification as n where n.user_id = :id and n.read = false and n.is_visible = true order by n.created_at desc", nativeQuery = true)
    Page<Notification> findAllByUserIdAndReadFalseAndVisibleOrderByCreatedAtDesc(
            @Param("id") Long id,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE notification
            SET read = true
            WHERE user_id = :userId
              AND read = false
            """, nativeQuery = true)
    void markAllAsRead(@Param("userId") Long userId);

}
