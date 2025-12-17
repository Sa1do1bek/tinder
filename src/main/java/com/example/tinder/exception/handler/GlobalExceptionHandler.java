package com.example.tinder.exception.handler;

import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.example.tinder.dto.ApiResponse;


import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(value = Integer.MIN_VALUE)
public class GlobalExceptionHandler {

    //Client side Exceptions
    @ExceptionHandler(value = {RestException.class})
    public ResponseEntity<?> handleException(RestException ex, HttpServletRequest request) {
        ErrorType error = ex.getErrorType();
        String path = request.getRequestURI();
        return new ResponseEntity<>(ApiResponse.error(error.getMsg(), error.getStatusValue(), path), error.getStatus());
    }

    //Internal Server Errors
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        log.error("Path: {} \nInternal Server Error: {}", request.getRequestURI(), ex.getMessage());
        ErrorType error = ErrorType.INTERNAL_ERROR;
        String path = request.getRequestURI();
        String msg = error.getMsg() + ": " + ex.getMessage();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.BAD_JSON;
        String path = request.getRequestURI();
        String msg = error.getMsg() + ": " + ex.getMessage();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.PAGE_NOT_FOUND;
        String path = request.getRequestURI();
        String msg = error.getMsg();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.UNSUPPORTED_MEDIA_TYPE;
        String path = request.getRequestURI();
        String msg = error.getMsg() + ": " + ex.getMessage();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.METHOD_NOT_ALLOWED;
        String path = request.getRequestURI();
        String msg = error.getMsg() + ": " + ex.getMethod();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.MISSING_PARAMETER;
        String path = request.getRequestURI();
        String msg = error.getMsg() + ": " + ex.getParameterName();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.TYPE_MISMATCH;
        String path = request.getRequestURI();
        String base = error.getMsg();
        String msg = base + ": " + ex.getName() + " -> expected " + ex.getRequiredType().getSimpleName();
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.VALIDATION_ERROR;
        String path = request.getRequestURI();
        String base = error.getMsg();

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " - " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String msg = details.isBlank() ? base : base + ": " + details;
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ErrorType error = ErrorType.VALIDATION_ERROR;
        String path = request.getRequestURI();
        String base = error.getMsg();

        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " - " + v.getMessage())
                .collect(Collectors.joining("; "));

        String msg = details.isBlank() ? base : base + " -> " + details;
        return new ResponseEntity<>(ApiResponse.error(msg, error.getStatusValue(), path), error.getStatus());
    }


}
