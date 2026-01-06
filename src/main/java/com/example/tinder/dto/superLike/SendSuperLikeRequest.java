package com.example.tinder.dto.superLike;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendSuperLikeRequest {
    private Long receiverId;
}