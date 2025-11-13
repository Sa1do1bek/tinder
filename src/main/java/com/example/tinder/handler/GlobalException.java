package com.example.tinder.handler;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.exception.BadRequestException;
import com.example.tinder.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalException {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(e.getMessage())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse handleResourceNotFoundException(ResourceNotFoundException e) {
        return ApiResponse.builder()
                .message(e.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    public ApiResponse handleBadRequestException(BadRequestException e) {
        return ApiResponse.builder()
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = "Ma'lumotlar butunligi xatosi. Ehtimol, noyob (unique) qiymat takrorlanmoqda.";

        if (e.getCause() != null && e.getCause().getMessage().contains("Duplicate entry")) {
            message = "Kiritilgan ma'lumot (masalan, Email/Username) bazada allaqachon mavjud.";
        }

        return ApiResponse.builder()
                .message(message)
                .status(HttpStatus.CONFLICT)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiResponse.builder()
                .message("So'rov tanasi (JSON) noto'g'ri formatlangan yoki o'qib bo'lmaydi. Iltimos, ma'lumot turlarini tekshiring.")
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse handleGeneralException(Exception e) {
        return ApiResponse.builder()
                .message("Kutilmagan server xatosi sodir bo'ldi. Iltimos, keyinroq urinib ko'ring.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
