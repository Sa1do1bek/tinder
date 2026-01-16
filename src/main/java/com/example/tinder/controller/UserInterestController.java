package com.example.tinder.controller;


import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.CategoryType;
import com.example.tinder.service.UserInterestService;
import com.example.tinder.utils.CurrentUser;
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

    @PutMapping("/updateUserCategory")
    public ApiResponse<?> updateUserCategory(@CurrentUser User user,
                                            @RequestParam("categoryIds") List<Long> categoryIds) {
        return userInterestService.updateUserCategory(user.getId(), categoryIds);
    }

    @GetMapping("/getUserCategories")
    public ApiResponse<?> getUserCategories(@CurrentUser User user) {
        return userInterestService.getUserCategories(user.getId());
    }


    @GetMapping("/getUserCategoriesByType")
    public ApiResponse<?> getUserCategoriesByType(@CurrentUser User user,
                                                  @RequestParam("type") CategoryType type) {

        return userInterestService.getUserCategoriesByType(user.getId(), type);
    }

    @GetMapping("/getAllUserCategories")
    public ApiResponse<?> getUserCategoriesByType(@CurrentUser User user) {
        return userInterestService.getAllUserCategoriesByUserId(user.getId());
    }
}
