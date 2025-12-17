package com.example.tinder.exception;

import com.example.tinder.enums.ErrorType;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RestException extends RuntimeException {

    private HttpStatus status;
    private final ErrorType errorType;


    public RestException(@NotNull ErrorType errorType) {
        this.errorType = errorType;
        this.status = errorType.getStatus();
    }

    private RestException(@NotNull ErrorType errorType, HttpStatus status) {
        this.errorType = errorType;
        this.status = status;
    }

    public static RestException restThrow(@NotNull ErrorType errorType) {
        return new RestException(errorType);
    }

    public static RestException restThrow(@NotNull ErrorType errorType, HttpStatus status) {
        return new RestException(errorType, status);
    }

    public static Supplier<RestException> thew(@NotNull ErrorType errorType) {
        return () -> new RestException(errorType);
    }

}