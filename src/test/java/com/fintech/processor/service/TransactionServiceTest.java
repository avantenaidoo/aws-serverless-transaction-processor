
package com.fintech.processor.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintech.processor.model.CurrencyCode;
import com.fintech.processor.model.Transaction;
import com.fintech.processor.model.TransactionRequest;
import com.fintech.processor.model.TransactionResponse;
import com.fintech.processor.model.TransactionStatus;
import com.fintech.processor.repository.TransactionRepository;
import com.fintech.processor.validation.TransactionValidator;

class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);

        transactionService = new TransactionService(
                transactionRepository,
                new TransactionValidator(),
                new TransactionReferenceGenerator());
    }

    @Test
    void shouldAcceptAndSaveNewTransaction() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("150.00"),
                "ZAR",
                "test-key-001");

        when(transactionRepository.findByIdempotencyKey("test-key-001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.saveIfAbsent(
                org.mockito.ArgumentMatchers.any(Transaction.class))).thenReturn(true);

        TransactionResponse response = transactionService.process(request);

        assertEquals(TransactionStatus.ACCEPTED, response.status());
        assertEquals(new BigDecimal("150.00"), response.amount());
        assertEquals("ZAR", response.currency());
        assertNotNull(response.transactionReference());

        verify(transactionRepository).saveIfAbsent(
                org.mockito.ArgumentMatchers.any(Transaction.class));
    }

    @Test
    void shouldReturnExistingTransactionForDuplicateRequest() {
        Transaction existingTransaction = new Transaction(
                "test-key-002",
                "TXN-existing-reference",
                new BigDecimal("200.00"),
                CurrencyCode.ZAR,
                Instant.parse("2026-11-13T10:00:00Z"));

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("200.00"),
                "ZAR",
                "test-key-002");

        when(transactionRepository.findByIdempotencyKey("test-key-002"))
                .thenReturn(Optional.of(existingTransaction));

        TransactionResponse response = transactionService.process(request);

        assertEquals(TransactionStatus.DUPLICATE, response.status());
        assertEquals(
                "TXN-existing-reference",
                response.transactionReference());
        assertEquals(new BigDecimal("200.00"), response.amount());

        verify(transactionRepository).findByIdempotencyKey("test-key-002");
    }

    @Test
    void shouldReturnExistingTransactionWhenConcurrentRequestWinsSave() {
        Transaction existingTransaction = new Transaction(
                "test-key-003",
                "TXN-original-reference",
                new BigDecimal("300.00"),
                CurrencyCode.ZAR,
                Instant.parse("2026-11-13T10:00:00Z"));

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("300.00"),
                "ZAR",
                "test-key-003");

        when(transactionRepository.findByIdempotencyKey("test-key-003"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingTransaction));

        when(transactionRepository.saveIfAbsent(
                org.mockito.ArgumentMatchers.any(Transaction.class))).thenReturn(false);

        TransactionResponse response = transactionService.process(request);

        assertEquals(TransactionStatus.DUPLICATE, response.status());
        assertEquals(
                "TXN-original-reference",
                response.transactionReference());
    }
}
