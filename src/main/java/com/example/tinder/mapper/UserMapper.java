package com.example.tinder.mapper;

import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.entity.*;
import com.example.tinder.enums.Interest;
import com.example.tinder.repository.RoleRepository;
import com.example.tinder.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final RoleRepository roleRepository;
    private  final UserLocationRepository userLocationRepository;

    public User toEntityByEmail(RegisterDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .fullname(dto.getFullName())
                .username(dto.getUsername())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .build();
    }

    public UserDto toDto(User user) {
        Optional<UserLocation> location = userLocationRepository.findByUserId(user.getId());

        List<UserRole> list = this.roleRepository.findByUserId(user.getId());
        List<Interest> interests = user.getUserInterests() != null
                ? user.getUserInterests().stream()
                .map(UserInterest::getName)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(String.valueOf(user.getStatus()))
                .role(list != null && !list.isEmpty() ?
                        list.stream().map(UserRole::getName).toList() : new ArrayList<>())
                .username(user.getUsername())
                .profilePic(user.getProfilePic())
                .birthDate(user.getBirthDate())
                .longitude(location.map(UserLocation::getLongitude).orElse(null))
                .latitude(location.map(UserLocation::getLatitude).orElse(null))
                .gender(user.getGender())
                .interests(interests)
                .bio(user.getBio())
                .build();
    }

    public List<UserDto> dtoList(List<User> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(this::toDto).toList();
        }
        return new ArrayList<>();
    }
}
