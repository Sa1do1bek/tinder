package com.example.tinder.repository;

import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.Interest;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    @Modifying
    @Transactional
    @Query("delete from UserInterest ui where ui.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}