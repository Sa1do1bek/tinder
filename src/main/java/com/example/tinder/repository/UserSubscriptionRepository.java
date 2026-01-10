package com.example.tinder.repository;

import com.example.tinder.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {


    @Query("""
    SELECT s FROM UserSubscription s
    WHERE s.visibility = true
    AND s.endAt <= :now
""")
    List<UserSubscription> findAllExpiredAndActive(LocalDateTime now);
}
