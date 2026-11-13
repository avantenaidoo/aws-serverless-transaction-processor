
package com.fintech.processor.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import com.fintech.processor.model.CurrencyCode;
import com.fintech.processor.model.Transaction;
import com.fintech.processor.model.TransactionRequest;
import com.fintech.processor.model.TransactionResponse;
import com.fintech.processor.model.TransactionStatus;
import com.fintech.processor.repository.TransactionRepository;
import com.fintech.processor.validation.TransactionValidator;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;
    private final TransactionReferenceGenerator referenceGenerator;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionValidator transactionValidator,
            TransactionReferenceGenerator referenceGenerator) {
        this.transactionRepository = transactionRepository;
        this.transactionValidator = transactionValidator;
        this.referenceGenerator = referenceGenerator;
    }

    public TransactionResponse process(TransactionRequest request) {

        transactionValidator.validate(request);

        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(
                request.idempotencyKey());

        if (existingTransaction.isPresent()) {
            return createResponse(
                    existingTransaction.get(),
                    TransactionStatus.DUPLICATE,
                    "Duplicate request. Existing transaction returned.");
        }

        Transaction transaction = new Transaction(
                request.idempotencyKey(),
                referenceGenerator.generate(),
                request.amount(),
                CurrencyCode.valueOf(
                        request.currency().trim().toUpperCase(Locale.ROOT)),
                Instant.now());

        boolean saved = transactionRepository.saveIfAbsent(transaction);

        if (!saved) {
            Transaction existing = transactionRepository
                    .findByIdempotencyKey(request.idempotencyKey())
                    .orElseThrow(() -> new IllegalStateException(
                            "Transaction could not be saved or retrieved."));

            return createResponse(
                    existing,
                    TransactionStatus.DUPLICATE,
                    "Duplicate request. Existing transaction returned.");
        }

        return createResponse(
                transaction,
                TransactionStatus.ACCEPTED,
                "Transaction accepted successfully.");
    }

    private TransactionResponse createResponse(
            Transaction transaction,
            TransactionStatus status,
            String message) {
        return new TransactionResponse(
                status,
                transaction.transactionReference(),
                transaction.amount(),
                transaction.currency().name(),
                message);
    }
}
