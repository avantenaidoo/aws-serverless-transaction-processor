package com.fintech.processor.repository;

import java.util.Optional;

import com.fintech.processor.model.Transaction;

public interface TransactionRepository {
    
    boolean saveIfAbsent(Transaction transaction);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
