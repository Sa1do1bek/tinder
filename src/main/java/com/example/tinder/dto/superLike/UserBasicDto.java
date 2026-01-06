package com.example.tinder.dto.superLike;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicDto {
    private Long id;
    private String username;
    private String fullname;
    private String profilePic;
}