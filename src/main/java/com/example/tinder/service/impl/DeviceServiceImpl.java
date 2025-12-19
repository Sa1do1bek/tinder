package com.example.tinder.service.impl;

import com.example.tinder.entity.Device;
import com.example.tinder.entity.User;
import com.example.tinder.repository.DeviceRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository trustedDeviceRepository;
    private final UserRepository userRepository;

    @Override
    public boolean isDeviceTrusted(Long userId, String deviceId) {
        Device device = trustedDeviceRepository.findByUserIdAndDeviceName(userId, deviceId).orElse(null);
        if (device == null) {
            return false;
        }
        if (device.getExpiredAt() != null && device.getExpiredAt().isBefore(LocalDateTime.now())) {
            device.setDeviceName(null);
            return false;
        }
        return true;
    }

    @Override
    public Device addTrustedDevice(Long userId, String deviceId) {
        Device devices = new Device();
        devices.setDeviceName(deviceId);
        devices.setAddedAt(LocalDateTime.now());
        devices.setIsVerified(true);
        User user = this.userRepository.findById(userId).orElse(null);
        if (user != null) {
            devices.setUser(user);
        }
        // 30 kundan keyin expire bo'lishi kerak
        devices.setExpiredAt(LocalDateTime.now().plusDays(30));

        return trustedDeviceRepository.save(devices);
    }

    @Override
    public Device addTrustedDeviceByLogin(Long userId, String deviceId) {
        Device devices = new Device();
        devices.setAddedAt(LocalDateTime.now());
        // 30 kundan keyin expire bo'lishi kerak
        devices.setExpiredAt(LocalDateTime.now().plusDays(30));

        return devices;
    }
}
