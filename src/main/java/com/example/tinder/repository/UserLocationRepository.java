package com.example.tinder.repository;

import com.example.tinder.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByUserId(Long userId);
}
