package com.example.tinder.dto;

import com.example.tinder.enums.CategoryType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInterestDto {
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;
    private String code;
    private String name;
    private String nameRu;
    private String nameUz;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInterestCreateDto{
        @Enumerated(EnumType.STRING)
        private CategoryType categoryType;
        private String code;
        private String name;
        private String nameRu;
        private String nameUz;
    }

}
