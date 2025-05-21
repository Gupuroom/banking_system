package com.example.demo.common.validation;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.error.CommonErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CommonValidator {
    public void validateAccountNumberFormat(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank() || accountNumber.length() > 20) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public void validateAmountFormat(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "금액은 필수입니다.");
        }

        if (amount.scale() > 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "금액은 정수만 입력 가능합니다.");
        }
    }

    public void validatePositiveAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "금액은 0보다 커야 합니다.");
        }
    }
} 