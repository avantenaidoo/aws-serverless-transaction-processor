package com.fintech.processor.model;

import java.math.BigDecimal;

public record TransactionResponse(
    TransactionStatus status,
    String transactionReference,
    BigDecimal amount,
    String currency,
    String message
) {
    
}
