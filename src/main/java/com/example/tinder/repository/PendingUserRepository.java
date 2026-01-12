package com.example.tinder.repository;

import com.example.tinder.entity.PendingUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
    Optional<PendingUser> findByEmail(@NotBlank String email);

    @Query("SELECT p FROM PendingUser p WHERE p.phoneNumber = :phoneNumber")
    Optional<PendingUser> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    void deleteByEmailOrUsername(@NotBlank(message = "email cannot be null or empty") @Email String email, String username);
}