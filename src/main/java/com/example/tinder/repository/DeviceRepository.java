package com.example.tinder.repository;

import com.example.tinder.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device,Long> {
    Optional<Device> findByUserIdAndDeviceName(Long userId, String deviceId);

}
