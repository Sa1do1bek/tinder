package com.example.tinder.repository;

import com.example.tinder.entity.UserInterest;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    @Modifying
    @Transactional
    @Query("delete from UserInterest ui where ui.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}