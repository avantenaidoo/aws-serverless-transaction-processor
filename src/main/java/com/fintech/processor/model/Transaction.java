package com.fintech.processor.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String idempotencyKey,
        String transactionReference,
        BigDecimal amount,
        CurrencyCode currency,
        Instant createdAt
) {
    
}
