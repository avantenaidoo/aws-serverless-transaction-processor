package com.fintech.processor.model;

import java.math.BigDecimal;

public record TransactionRequest(
    BigDecimal amount,
    String currency,
    String idempotencyKey
) {
    
}
