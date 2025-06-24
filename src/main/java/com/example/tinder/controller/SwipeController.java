package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.enums.SwipeStatus;
import com.example.tinder.service.SwipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/swipe")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping("/swipeUser")
    public ApiResponse swipeUser(@RequestParam("swiperId") Long swiperId,
                                 @RequestParam("swipedId")Long swipedId,
                                 @RequestParam("status")SwipeStatus status
                                 ){
        return swipeService.swipe(swiperId,swipedId,status);
    }

    @GetMapping("/getSwipesByUser")
    public ApiResponse getSwipesByUser(@RequestParam("userId") Long userId){
        return swipeService.getSwipesByUser(userId);
    }

}
