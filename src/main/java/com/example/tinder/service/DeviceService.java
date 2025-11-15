package com.example.tinder.service;

import com.example.tinder.entity.Device;
import org.springframework.stereotype.Component;

@Component
public interface DeviceService {

    boolean isDeviceTrusted(Long userId, String deviceId);

    Device addTrustedDevice(Long userId, String deviceId, String fcmToken);

    Device addTrustedDeviceByLogin(Long userId, String deviceId);

}
