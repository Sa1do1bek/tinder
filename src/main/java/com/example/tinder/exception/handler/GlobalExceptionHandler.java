package com.example.tinder.exception.handler;

import com.example.tinder.dto.payment.click.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.payme.res.PaymeError;
import com.example.tinder.dto.payment.payme.res.PaymeErrorResponse;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.ClickException;
import com.example.tinder.exception.PaymeException;
import com.example.tinder.exception.PaymentException;
import com.example.tinder.exception.RestException;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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
import com.example.tinder.dto.base.ApiResponse;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(value = Integer.MIN_VALUE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ClickException.class)
    public ClickCompleteResponse handleException(ClickException e) {
        log.error("Error occurred: {}", e.getCode().getErrorNote());

        return ClickCompleteResponse.builder()
                .error(e.getCode().getCode())
                .errorNote(e.getCode().getErrorNote())
                .build();
    }

    @ExceptionHandler(PaymeException.class)
    public PaymeErrorResponse handlePaymeException(PaymeException e) {
        return new PaymeErrorResponse(new PaymeError(e.getError(), e.getData()));
    }

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


    /**
     * Firebase Authentication xatolarni handle qilish
     */
    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<?> handleFirebaseAuthException(FirebaseAuthException e) {
        log.error("🔥 Firebase Auth Exception caught in global handler");
        log.error("   Error Code: {}", e.getAuthErrorCode());
        log.error("   Message: {}", e.getMessage());

        String userMessage;
        HttpStatus status;

        switch (e.getAuthErrorCode().name()) {
            case "EXPIRED_ID_TOKEN":
                userMessage = "Token muddati tugagan. Iltimos qayta login qiling.";
                status = HttpStatus.UNAUTHORIZED;
                break;

            case "INVALID_ID_TOKEN":
                userMessage = "Token noto'g'ri formatda. Iltimos yangi token bilan urinib ko'ring.";
                status = HttpStatus.BAD_REQUEST;
                break;

            case "REVOKED_ID_TOKEN":
                userMessage = "Token bekor qilingan. Iltimos qayta login qiling.";
                status = HttpStatus.UNAUTHORIZED;
                break;

            case "USER_DISABLED":
                userMessage = "Hisobingiz o'chirilgan yoki bloklangan.";
                status = HttpStatus.FORBIDDEN;
                break;

            case "USER_NOT_FOUND":
                userMessage = "Foydalanuvchi topilmadi.";
                status = HttpStatus.NOT_FOUND;
                break;

            case "INVALID_CREDENTIAL":
                userMessage = "Autentifikatsiya ma'lumotlari noto'g'ri.";
                status = HttpStatus.UNAUTHORIZED;
                break;

            case "PROJECT_NOT_FOUND":
                userMessage = "Firebase project topilmadi. Server xatosi.";
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;

            case "INSUFFICIENT_PERMISSION":
                userMessage = "Ruxsat etilmagan. Iltimos admin bilan bog'laning.";
                status = HttpStatus.FORBIDDEN;
                break;

            default:
                userMessage = "Autentifikatsiya xatosi: " + e.getMessage();
                status = HttpStatus.UNAUTHORIZED;
        }

        return buildErrorResponse(status, userMessage, e.getAuthErrorCode().name(), e.getMessage());
    }

    /**
     * IllegalStateException (token verification failed)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        log.error("❌ IllegalStateException: {}", e.getMessage(), e);

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Token verification xatosi",
                "VERIFICATION_FAILED",
                e.getMessage()
        );
    }

    /**
     * IllegalArgumentException (invalid token format)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("❌ IllegalArgumentException: {}", e.getMessage(), e);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Noto'g'ri so'rov",
                "INVALID_REQUEST",
                e.getMessage()
        );
    }


    /**
     * Error response yaratish helper method
     */
    private ResponseEntity<?> buildErrorResponse(
            HttpStatus status,
            String message,
            String errorCode,
            String details) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("details", details);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException ex) {
        log.error("Payment error: {} (code: {})", ex.getMessage(), ex.getErrorCode());

        return ResponseEntity.ok(Map.of(
                "error", ex.getErrorCode(),
                "error_note", ex.getMessage()
        ));
    }


}
