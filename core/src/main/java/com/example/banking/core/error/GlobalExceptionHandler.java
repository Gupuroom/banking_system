package com.example.banking.core.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(new ErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE));
    }
} 