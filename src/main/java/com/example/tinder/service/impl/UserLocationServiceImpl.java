package com.example.tinder.service.impl;

import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.UserLocationDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Match;
import com.example.tinder.entity.ProximityNotification;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.NotificationPathType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.MatchRepository;
import com.example.tinder.repository.ProximityNotificationRepository;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.NotificationService;
import com.example.tinder.service.UserLocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationServiceImpl implements UserLocationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserLocationRepository userLocationRepository;
    private final MatchRepository matchRepository;
    private final NotificationService notificationService;
    private final ProximityNotificationRepository proximityNotificationRepository;

    @Override
    @Transactional
    public ApiResponse<?> update(Long userId, UserLocationDto userLocationDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        user.setLatitude(userLocationDto.getLatitude());
        user.setLongitude(userLocationDto.getLongitude());
        userRepository.save(user);

        UserLocation userLocation = userLocationRepository.findByUserId(userId)
                .orElse(UserLocation.builder().user(user).build());
        userLocation.setUser(user);
        userLocation.setLatitude(userLocationDto.getLatitude());
        userLocation.setLongitude(userLocationDto.getLongitude());
        userLocationRepository.save(userLocation);

        double lat1 = Double.parseDouble(userLocationDto.getLatitude());
        double lon1 = Double.parseDouble(userLocationDto.getLongitude());

        log.info("Location yangilandi → User: {} | Lat: {} | Lon: {}", userId, lat1, lon1);

        checkNearbyMatches(user, lat1, lon1);

        return ApiResponse.success(SuccessType.USER_LOCATION_UPDATED_SUCCESSFULLY);
    }

    private void checkNearbyMatches(User currentUser, double lat1, double lon1) {
        log.info("=== checkNearbyMatches BOSHlandi === User: {}", currentUser.getId());

        List<Match> matches = matchRepository.findUserMatches(currentUser.getId());
        log.info("Topilgan matchlar soni: {}", matches.size());

        for (Match match : matches) {
            User otherUser = match.getUser1().getId().equals(currentUser.getId())
                    ? match.getUser2() : match.getUser1();

            log.info("Tekshirilmoqda → User{} <-> User{}", currentUser.getId(), otherUser.getId());

            if (!otherUser.isActive()) {
                log.info("User {} inactive → o'tkazib yuborildi", otherUser.getId());
                continue;
            }

            Optional<UserLocation> otherLocOpt = userLocationRepository.findByUserId(otherUser.getId());
            if (otherLocOpt.isEmpty()) {
                log.info("User {} uchun location topilmadi", otherUser.getId());
                continue;
            }

            UserLocation otherLoc = otherLocOpt.get();
            if (otherLoc.getLatitude() == null || otherLoc.getLongitude() == null) {
                log.info("User {} location maydonlari null", otherUser.getId());
                continue;
            }

            try {
                double lat2 = Double.parseDouble(otherLoc.getLatitude());
                double lon2 = Double.parseDouble(otherLoc.getLongitude());

                double distance = haversine(lat1, lon1, lat2, lon2);
                double distanceMeters = distance * 1000;

                log.info("Masofa hisoblandi → User{} <-> User{} = {} metr",
                        currentUser.getId(), otherUser.getId(), (int) distanceMeters);

                if (distance <= 0.5) {
                    log.info("✅ 500 METR ICHIDA! Notification jarayoni boshlanmoqda...");
                    sendIfCooldownPassed(currentUser, otherUser);
                } else {
                    log.info("Masofa 500 metrdan tashqarida");
                }
            } catch (Exception e) {
                log.error("Location parse xatosi User: {}", otherUser.getId(), e);
            }
        }

        log.info("=== checkNearbyMatches TUGADI ===");
    }

    private void sendIfCooldownPassed(User user1, User user2) {
        Long uA = Math.min(user1.getId(), user2.getId());
        Long uB = Math.max(user1.getId(), user2.getId());

        log.info("sendIfCooldownPassed ishga tushdi → {} <-> {}", uA, uB);

        Optional<ProximityNotification> opt = proximityNotificationRepository.findByUserPair(uA, uB);

        LocalDateTime now = LocalDateTime.now();

        if (opt.isPresent()) {
            ProximityNotification pn = opt.get();
            if (pn.getLastNotifiedAt().isAfter(now.minusMinutes(30))) {
                log.info("⛔ Cooldown hali faol → Notification yuborilmaydi");
                return;
            }
            pn.setLastNotifiedAt(now);
            proximityNotificationRepository.save(pn);
            log.info("Cooldown yangilandi");
        } else {
            ProximityNotification pn = ProximityNotification.builder()
                    .user1(user1)
                    .user2(user2)
                    .lastNotifiedAt(now)
                    .build();
            proximityNotificationRepository.save(pn);
            log.info("Yangi ProximityNotification yozuvi yaratildi");
        }

        sendNearbyNotification(user1, user2);
    }

    private void sendNearbyNotification(User user1, User user2) {
        String title = "LuVi 🔥";
        String body1 = user2.getFullname() + " sizga juda yaqin (500 metr ichida)!";
        String body2 = user1.getFullname() + " sizga juda yaqin (500 metr ichida)!";

        log.info("Notification yuborishga urinilmoqda:");
        log.info("→ User{} : {}", user1.getId(), body1);
        log.info("→ User{} : {}", user2.getId(), body2);

        try {
            notificationService.sendNotification(user1.getId(), title, body1, NotificationPathType.NEWS_MESSAGE);
            notificationService.sendNotification(user2.getId(), title, body2, NotificationPathType.NEWS_MESSAGE);
            log.info("✅ Notification ikkala tomonga ham yuborildi!");
        } catch (Exception e) {
            log.error("❌ Notification yuborishda xatolik!", e);
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    @Override
    public List<UserDto> getNearbyUsers(Long userId, double radiusKm) {
        // Eski kodni saqlab qoldim
        UserLocation userLocation = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_LOCATION_NOT_FOUND));

        if (userLocation.getLatitude() == null || userLocation.getLongitude() == null) {
            throw RestException.restThrow(ErrorType.USER_LOCATION_NOT_FOUND);
        }

        double lat1 = Double.parseDouble(userLocation.getLatitude());
        double lon1 = Double.parseDouble(userLocation.getLongitude());

        return userLocationRepository.findAll()
                .stream()
                .filter(loc -> !loc.getUser().getId().equals(userId))
                .filter(loc -> loc.getUser().isActive())
                .filter(loc -> isValidLocation(loc, lat1, lon1, radiusKm))
                .map(loc -> userMapper.toDto(loc.getUser()))
                .toList();
    }

    private boolean isValidLocation(UserLocation loc, double lat1, double lon1, double radiusKm) {
        if (loc.getLatitude() == null || loc.getLongitude() == null) return false;
        try {
            double lat2 = Double.parseDouble(loc.getLatitude());
            double lon2 = Double.parseDouble(loc.getLongitude());
            return haversine(lat1, lon1, lat2, lon2) <= radiusKm;
        } catch (Exception e) {
            return false;
        }
    }
}