package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.CategoryType;
import com.example.tinder.service.DiscoveryService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    @GetMapping("/find")
    public ApiResponse<?> findUser(@CurrentUser User currentUser,
                                       @RequestParam(required = false, defaultValue = "0") Integer page,
                                       @RequestParam(required = false, defaultValue = "20") Integer size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return discoveryService.getDiscoveryUsers(currentUser.getId(), pageable);
    }


    @GetMapping("/findByCategory")
    public ApiResponse<?> findByCategory(@CurrentUser User currentUser,
                                   @RequestParam("categoryId") Long categoryId,
                                   @RequestParam(required = false, defaultValue = "0") Integer page,
                                   @RequestParam(required = false, defaultValue = "20") Integer size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return discoveryService.findAllUsersByUserInterests(currentUser.getId(),categoryId, pageable);
    }







}