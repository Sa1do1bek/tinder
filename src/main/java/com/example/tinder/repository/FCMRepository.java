package com.example.tinder.repository;

import com.example.tinder.entity.FCM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMRepository extends JpaRepository<FCM, Long> {

    @Query(value = "select * from fcm as f where f.id = :id and f.is_visible = true", nativeQuery = true)
    Optional<FCM> findByIdAndVisibilityTrue(@Param("id") Long id);

    @Query(value = "select * from fcm as f where f.is_visible = true", nativeQuery = true)
    Page<FCM> findAllByVisibilityTrue(Pageable pageable);

    @Query(value = "select * from fcm as f where f.user_id = :userId and f.is_visible = true", nativeQuery = true)
    Optional<FCM> findByUserIdAndVisibilityTrue(@Param("userId") Long userId);
}