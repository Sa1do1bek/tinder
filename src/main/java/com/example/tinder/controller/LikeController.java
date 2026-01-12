package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.LikeService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController{
    private final LikeService userService;

    @GetMapping("/getRejectedUsers")
    public ApiResponse<?> getAllRejectedUsers(@CurrentUser User user,
                                              @RequestParam(defaultValue = "0")int page,
                                              @RequestParam(defaultValue = "5") int size){
        return userService.getAllRejectUsers(PageRequest.of(page,size),user.getId());
    }

    @GetMapping("/getMyLikedUsers")
    public ApiResponse<?> getMyLikedUsers(@CurrentUser User user,
                                              @RequestParam(defaultValue = "0")int page,
                                              @RequestParam(defaultValue = "5") int size){
        return userService.getMyLikedUsers(PageRequest.of(page,size),user.getId());
    }


    @GetMapping("/getMyLovers")
    public ApiResponse<?> getMyLovers(@CurrentUser User user,
                                          @RequestParam(defaultValue = "0")int page,
                                          @RequestParam(defaultValue = "5") int size){
        return userService.getMyLovers(PageRequest.of(page,size),user.getId());
    }


    @GetMapping("/getTopLikedUsers")
    public ApiResponse<?> getTopLikedUsers(@CurrentUser User user){
        return userService.getTopLikedUsers(user.getId());
    }

    @GetMapping("/getLeastLikedUsers")
    public ApiResponse<?> getLeastLikedUsers(@CurrentUser User user){
        return userService.getLeastLikedUsers(user.getId());
    }





}
