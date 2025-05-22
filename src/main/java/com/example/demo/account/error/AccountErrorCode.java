package com.example.demo.account.error;

import com.example.banking.core.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountErrorCode implements ErrorCode {

    ACCOUNT_NOT_FOUND("A001", "계좌를 찾을 수 없습니다."),
    ACCOUNT_ALREADY_EXISTS("A002", "이미 존재하는 계좌번호입니다."),
    ACCOUNT_NUMBER_BLANK("A003", "계좌번호는 필수입니다."),
    ACCOUNT_ALREADY_DELETED("A004", "이미 삭제된 계좌입니다."),
    INSUFFICIENT_BALANCE("A005", "잔액이 부족합니다."),
    INVALID_AMOUNT("A006", "유효하지 않은 금액입니다. (0보다 커야 합니다)"),
    INVALID_ACCOUNT_STATUS("A007", "유효하지 않은 계좌 상태입니다."),
    ACCOUNT_TYPE_NOT_FOUND("A008", "계좌 타입을 찾을 수 없습니다.");

    private final String code;
    private final String message;
}