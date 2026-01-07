package com.example.tinder.enums;

import lombok.Getter;

@Getter
public enum SuccessType {
    SUCCESS("success"),
    SUCCESSFULLY_CREATED("successfully.created"),
    SUPER_LIKES_PURCHASED_SUCCESSFULLY("superlikes.purchased.successfullly");

    private final String key;
    SuccessType(String s) {
        this.key = s;
    }
}
