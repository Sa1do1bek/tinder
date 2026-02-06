package com.example.tinder.exception;

import com.example.tinder.enums.ClickResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClickException extends RuntimeException {
    private final ClickResponseCode code;
}
