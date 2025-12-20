package com.example.tinder.controller;


import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.UserInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interest")
public class UserInterestController {
    private final UserInterestService userInterestService;

   @GetMapping("/getAllCategoryTypes")
   public ApiResponse<?> getInterests() {
       return userInterestService.getAllCategoryTypes();
   }
}
