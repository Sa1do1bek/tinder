package com.example.tinder.mapper;

import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserRole;
import com.example.tinder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final RoleRepository roleRepository;
    public User toEntityByEmail(RegisterDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .fullname(dto.getFullName())
                .username(dto.getUsername())
                .build();
    }

    public UserDto toDto(User user) {
        List<UserRole> list = this.roleRepository.findByUserId(user.getId());

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(String.valueOf(user.getStatus()))
                .role(list != null && !list.isEmpty() ?
                        list.stream().map(UserRole::getName).toList() : new ArrayList<>())
                .username(user.getUsername())
                .profilePic(user.getProfilePic())
                .build();
    }

    public List<UserDto> dtoList(List<User> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(this::toDto).toList();
        }
        return new ArrayList<>();
    }
}
