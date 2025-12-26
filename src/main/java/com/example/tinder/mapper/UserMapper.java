package com.example.tinder.mapper;

import com.example.tinder.dto.UserDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private  final UserLocationRepository userLocationRepository;

    public UserDto toDto(User user) {
        Optional<UserLocation> location = userLocationRepository.findByUserId(user.getId());

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .role(user.getRole())
                .username(user.getUsername())
                .profilePic(user.getProfilePic())
                .birthDate(user.getBirthDate())
                .longitude(location.map(UserLocation::getLongitude).orElse(null))
                .latitude(location.map(UserLocation::getLatitude).orElse(null))
                .gender(user.getGender())
                .bio(user.getBio())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .build();
    }

    public List<UserDto> dtoList(List<User> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(this::toDto).toList();
        }
        return new ArrayList<>();
    }
}
