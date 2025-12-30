package com.example.tinder.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockDto {
    private Long id;
    private Long blockerId;
    private Long blockedId;

}
