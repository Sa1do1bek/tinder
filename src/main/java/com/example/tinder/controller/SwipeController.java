package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.SwipeStatus;
import com.example.tinder.service.SwipeService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/swipe")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping("/swipeUser")
    public ApiResponse<?> swipeUser(@CurrentUser User user,
                                 @RequestParam("swipedId")Long swipedId,
                                 @RequestParam("status")SwipeStatus status
                                 ){
        return swipeService.swipe(user.getId(), swipedId,status);
    }

    @GetMapping("/getSwipesByUser")
    public ApiResponse<?> getSwipesByUser(@CurrentUser User user){
        return swipeService.getSwipesByUser(user.getId());
    }



    @DeleteMapping("/unlike")
    public ApiResponse<?> unlike(@CurrentUser User user,
                                 @RequestParam Long swipedId) {
        return swipeService.unlike(user.getId(), swipedId);
    }
}
