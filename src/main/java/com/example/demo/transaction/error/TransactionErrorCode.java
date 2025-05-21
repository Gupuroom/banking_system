package com.example.demo.transaction.error;

import com.example.demo.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {
    INSUFFICIENT_BALANCE("T001", "거래를 위한 잔액이 부족합니다"),
    DAILY_WITHDRAWAL_LIMIT_EXCEEDED("T002", "일일 출금 한도를 초과했습니다"),
    DAILY_TRANSFER_LIMIT_EXCEEDED("T003", "일일 이체 한도를 초과했습니다"),
    INVALID_AMOUNT("T004", "유효하지 않은 거래 금액입니다 (0보다 커야 합니다)"),
    SAME_ACCOUNT_TRANSFER("T005", "자기 자신의 계좌로는 이체할 수 없습니다"),
    NOT_FOUND("T006", "거래 내역을 찾을 수 없습니다"),
    AMOUNT_TOO_SMALL("T007", "거래 금액이 최소 금액보다 작습니다"),
    AMOUNT_TOO_LARGE("T008", "거래 금액이 최대 금액을 초과합니다"),
    FAILED("T009", "거래 처리 중 오류가 발생했습니다");

    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return code;
    }
}