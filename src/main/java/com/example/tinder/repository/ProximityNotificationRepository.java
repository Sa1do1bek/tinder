package com.example.tinder.repository;

import com.example.tinder.entity.ProximityNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProximityNotificationRepository extends JpaRepository<ProximityNotification,Long> {

    @Query("""
        SELECT p FROM ProximityNotification p 
        WHERE (p.user1.id = :u1 AND p.user2.id = :u2) 
           OR (p.user1.id = :u2 AND p.user2.id = :u1)
    """)
    Optional<ProximityNotification> findByUserPair(
            @Param("u1") Long u1,
            @Param("u2") Long u2);
}
