package com.example.tinder.controller;


import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.interest.UserInterestDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.CategoryType;
import com.example.tinder.service.UserInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interest")
public class UserInterestController {
    private final UserInterestService userInterestService;

    @PostMapping("add")
    public ApiResponse<?> addUserInterest(@RequestBody UserInterestDto.UserInterestCreateDto dto) {
        return userInterestService.addCategory(dto);
    }

   @GetMapping("/getAllCategoryTypes")
   public ApiResponse<?> getInterests() {
       return userInterestService.getAllCategoryTypes();
   }


    @GetMapping("/getCategoriesByType")
   public ApiResponse<?> getCategoriesByType(@RequestParam CategoryType type) {
       return userInterestService.getAllCategoriesByType(type);
   }

    @PostMapping("/addCategoryToUser")
    public ApiResponse<?> addCategoryToUser(@RequestParam("userId") Long userId,
                                            @RequestParam("categoryIds") List<Long> categoryIds) {
        return userInterestService.addCategoryToUser(userId, categoryIds);
    }

    @GetMapping("/getUserCategories")
    public ApiResponse<?> getUserCategories(@RequestParam("userId") Long userId) {
        return userInterestService.getUserCategories(userId);
    }


    @GetMapping("/getUserCategoriesByType")
    public ApiResponse<?> getUserCategoriesByType(@RequestParam("userId") Long userId,
                                                  @RequestParam("type") CategoryType type) {

        return userInterestService.getUserCategoriesByType(userId, type);
    }



}
