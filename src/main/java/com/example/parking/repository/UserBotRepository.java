package com.example.parking.repository;

import com.example.parking.entity.UserBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBotRepository extends JpaRepository<UserBot, Long> {
    Optional<UserBot> findByPhoneNumber(String phoneNumber);
    Optional<UserBot> findByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
}
