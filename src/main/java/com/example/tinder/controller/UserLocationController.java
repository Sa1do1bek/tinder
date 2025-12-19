package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.UserLocationDto;
import com.example.tinder.entity.User;
import com.example.tinder.service.UserLocationService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class UserLocationController {
  private final UserLocationService userLocationService;

    @PostMapping("/update")
    public ApiResponse<?> update(@CurrentUser User user, UserLocationDto userLocationDto){
        return userLocationService.update(user.getId(),userLocationDto);
    }

    @GetMapping("/nearby")
    public List<UserDto> nearby(@CurrentUser User user, @RequestParam(defaultValue="50")double radiusKm){
        return userLocationService.getNearbyUsers(user.getId(),radiusKm);

    }
}
