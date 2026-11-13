
package com.fintech.processor.validation;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fintech.processor.model.TransactionRequest;

class TransactionValidatorTest {

    private TransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
    }

    @Test
    void shouldAcceptValidTransactionRequest() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("150.00"),
                "ZAR",
                "test-key-001");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(null));
    }

    @Test
    void shouldRejectZeroOrNegativeAmount() {
        TransactionRequest zeroAmount = new TransactionRequest(
                BigDecimal.ZERO, "ZAR", "test-key-002");

        TransactionRequest negativeAmount = new TransactionRequest(
                new BigDecimal("-10.00"), "ZAR", "test-key-003");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(zeroAmount));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(negativeAmount));
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "AUD",
                "test-key-004");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void shouldRejectAmountWithTooManyDecimalPlaces() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.123"),
                "ZAR",
                "test-key-005");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void shouldRejectMissingCurrency() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                " ",
                "test-key-006");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "ZAR",
                " ");

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
    }

    @Test
    void shouldRejectIdempotencyKeyLongerThan128Characters() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "ZAR",
                "a".repeat(129));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
    }
}
