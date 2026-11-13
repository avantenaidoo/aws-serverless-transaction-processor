
package com.fintech.processor.validation;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import com.fintech.processor.model.CurrencyCode;
import com.fintech.processor.model.TransactionRequest;

public class TransactionValidator {

    public void validate(TransactionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request is required.");
        }

        if (request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero.");
        }

        if (request.currency() == null
                || request.currency().isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required.");
        }

        CurrencyCode currency;

        try {
            currency = CurrencyCode.valueOf(
                    request.currency().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported currency. Supported currencies: ZAR, USD, EUR, GBP.");
        }

        int allowedDecimalPlaces = Currency
                .getInstance(currency.name())
                .getDefaultFractionDigits();

        if (request.amount().stripTrailingZeros().scale() > allowedDecimalPlaces) {
            throw new IllegalArgumentException(
                    "Amount has too many decimal places for " + currency + ".");
        }

        if (request.idempotencyKey() == null
                || request.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required.");
        }

        if (request.idempotencyKey().length() > 128) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 128 characters.");
        }
    }
}
