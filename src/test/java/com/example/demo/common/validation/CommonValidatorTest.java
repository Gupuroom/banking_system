package com.example.demo.common.validation;

import com.example.demo.common.error.BusinessException;
import com.example.demo.common.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonValidatorTest {

    private final CommonValidator validator = new CommonValidator();

    @Nested
    @DisplayName("계좌번호 형식 검증")
    class AccountNumberFormatValidation {
        @Test
        @DisplayName("올바른 계좌번호 형식 검증 성공")
        void validateAccountNumberFormat_success() {
            // given
            String[] validAccountNumbers = {
                "13-12-123456",
                "1312123456",
                "13-12-123456",
                "1234567890",
                "12345678901234567890"
            };

            // when & then
            for (String accountNumber : validAccountNumbers) {
                assertThatCode(() -> validator.validateAccountNumberFormat(accountNumber))
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("잘못된 계좌번호 형식 검증 실패")
        void validateAccountNumberFormat_failure() {
            // given
            String[] invalidAccountNumbers = {
                null,
                "",
                " "
            };

            // when & then
            for (String accountNumber : invalidAccountNumbers) {
                assertThatThrownBy(() -> validator.validateAccountNumberFormat(accountNumber))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    @Nested
    @DisplayName("금액 형식 검증")
    class AmountFormatValidation {
        @Test
        @DisplayName("올바른 금액 형식 검증 성공")
        void validateAmountFormat_success() {
            // given
            BigDecimal[] validAmounts = {
                new BigDecimal("1000"),
                new BigDecimal("0"),
                new BigDecimal("-1000")
            };

            // when & then
            for (BigDecimal amount : validAmounts) {
                assertThatCode(() -> validator.validateAmountFormat(amount))
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("잘못된 금액 형식 검증 실패")
        void validateAmountFormat_failure() {
            // given
            BigDecimal[] invalidAmounts = {
                null,
                new BigDecimal("1000.1"),
                new BigDecimal("1000.01"),
                new BigDecimal("0.1"),
                new BigDecimal("-0.1")
            };

            // when & then
            for (BigDecimal amount : invalidAmounts) {
                assertThatThrownBy(() -> validator.validateAmountFormat(amount))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    @Nested
    @DisplayName("양수 금액 검증")
    class PositiveAmountValidation {
        @Test
        @DisplayName("양수 금액 검증 성공")
        void validatePositiveAmount_success() {
            // given
            BigDecimal[] positiveAmounts = {
                new BigDecimal("1"),
                new BigDecimal("1000"),
                new BigDecimal("999999999")
            };

            // when & then
            for (BigDecimal amount : positiveAmounts) {
                assertThatCode(() -> validator.validatePositiveAmount(amount))
                    .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("음수 또는 0 금액 검증 실패")
        void validatePositiveAmount_failure() {
            // given
            BigDecimal[] nonPositiveAmounts = {
                BigDecimal.ZERO,
                new BigDecimal("-1"),
                new BigDecimal("-1000")
            };

            // when & then
            for (BigDecimal amount : nonPositiveAmounts) {
                assertThatThrownBy(() -> validator.validatePositiveAmount(amount))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
} 