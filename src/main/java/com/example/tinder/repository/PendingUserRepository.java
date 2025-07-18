package com.example.tinder.repository;

import com.example.tinder.entity.PendingUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
  Optional<PendingUser> findByEmail(@NotBlank String email);

    void deleteByEmailOrUsername(@NotBlank(message = "email cannot be null or empty") @Email String email, String username);
}