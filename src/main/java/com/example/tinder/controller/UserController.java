package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.enums.Gender;
import com.example.tinder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/addRoleToUser")
    public ApiResponse addRoleToUser(@RequestParam("userId") Long userId,
                                     @RequestParam("roleId") Long roleId) {
        return this.userService.addRoleToUser(userId, roleId);
    }
    @DeleteMapping("/deleteRoleFromUser")
    public ApiResponse deleteRoleFromUser(@RequestParam("userId") Long userId,
                                          @RequestParam("roleId") Long roleId) {
        return this.userService.deleteRoleFromUser(userId, roleId);
    }

    @GetMapping("/getUserById")
    public ApiResponse getUserById(@RequestParam("id") Long id) {
        return this.userService.getUserById(id);
    }


    @PutMapping("/updateUserNameAndFullName")
    public ApiResponse updateUserNameAndFullName(@RequestParam("userId") Long userId,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("fullname") String fullname) {
        return this.userService.updateUserNameAndFullName(userId, username, fullname);
    }
    @PutMapping("/updateEmail")
    public ApiResponse updateEmail(@RequestParam("email") String email,
                                   @RequestParam("newEmail") String newEmail) {
        return this.userService.updateEmail(email, newEmail);
    }


//    @PutMapping("/updateProfilePhoto")
//    public ApiResponse updateProfilePhoto(@RequestParam Long attachmentId,
//                                          @RequestParam Long userId) {
//        return this.userService.updateProfilePhoto(attachmentId,userId);
//    }

    @PostMapping("/checkUpdatedEmail")
    public ApiResponse checkUpdatedEmail(@RequestParam("oldEmail") String oldEmail,
                                         @RequestParam("newEmail") String newEmail,
                                         @RequestParam("code") String code) {
        return this.userService.checkUpdatedEmail(oldEmail, newEmail, code);
    }

    @PutMapping("/updatePassword")
    public ApiResponse updatePassword(@RequestParam("email") String email,
                                      @RequestParam("newPassword") String newPassword) {
        return this.userService.updatePassword(email, newPassword);
    }
    @PostMapping("/checkUpdatedPassword")
    public ApiResponse checkUpdatedPassword(@RequestParam("email") String email,
                                            @RequestParam("password") String password,
                                            @RequestParam("code") String code) {
        return this.userService.checkUpdatedPassword(email, password, code);
    }
    @PostMapping("/checkUsername")
    public ApiResponse checkUsername(@RequestParam("username") String username) {
        return this.userService.checkUsername(username);
    }
    @GetMapping("/getAllActiveUser")
    public ApiResponse getAllActiveUser(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                        @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        return this.userService.getAllActiveUser(PageRequest.of(page, size));
    }


    @GetMapping("/getUserByToken")
    public ApiResponse getUserByToken(@RequestParam("token") String token) {
        return this.userService.getUserByToken(token);
    }


    @PostMapping("/googleAuth")
    public ApiResponse googleAuth(@RequestParam("email") String email,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("picPath") String picPath) {
        return userService.googleAuth(email, fullName, picPath);
    }

    @GetMapping("/search")
    public ApiResponse search(@RequestParam(required = false)String username, @RequestParam(required = false) Gender gender){
        return userService.search(username,gender);
    }

    @PutMapping("/updateGenderOrBrithDateOrInterest")
    public ApiResponse updateUserGenderOrBrithdateOrInterest(@RequestParam("userId") Long userId, @RequestBody UpdateUserGBIDto dto) {
        return userService.updateUserGBI(userId, dto);
    }

}
