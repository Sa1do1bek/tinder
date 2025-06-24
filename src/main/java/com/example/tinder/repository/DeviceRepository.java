package com.example.tinder.repository;

import com.example.tinder.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findFirstByUserIdAndDeviceName(Long userId, String deviceName);

    List<Device> findAllByUserIdAndDeviceName(Long userId, String deviceName);

    // Eski metod (muammoli)
     Optional<Device> findByUserIdAndDeviceName(Long userId, String deviceName);
}
