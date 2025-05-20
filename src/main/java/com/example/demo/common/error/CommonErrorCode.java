package com.example.demo.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE("C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("C002", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;
}