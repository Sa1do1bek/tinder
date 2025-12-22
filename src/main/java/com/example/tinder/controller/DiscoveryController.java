package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.DiscoveryService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    @GetMapping("/find")
    public ApiResponse<?> getHomeUsers(@CurrentUser User currentUser,
                                       @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return discoveryService.getDiscoveryUsers(currentUser.getId(), pageable);
    }
}