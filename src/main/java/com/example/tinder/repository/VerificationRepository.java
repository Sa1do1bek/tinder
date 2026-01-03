package com.example.tinder.repository;

import com.example.tinder.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

    @Query("select v from Verification v where v.user.id = :userId")
    Optional<Verification> findByUserId(@Param("userId") Long userId);

}
