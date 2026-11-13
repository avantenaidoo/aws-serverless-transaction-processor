
package com.fintech.processor.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintech.processor.model.CurrencyCode;
import com.fintech.processor.model.Transaction;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoDbTransactionRepositoryTest {

    private DynamoDbClient dynamoDbClient;
    private DynamoDbTransactionRepository repository;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        repository = new DynamoDbTransactionRepository(
                dynamoDbClient,
                "transactions");
    }

    @Test
    void shouldSaveTransactionWhenIdempotencyKeyDoesNotExist() {
        Transaction transaction = new Transaction(
                "test-key-001",
                "TXN-reference-001",
                new BigDecimal("150.00"),
                CurrencyCode.ZAR,
                Instant.parse("2026-11-13T10:00:00Z"));

        assertTrue(repository.saveIfAbsent(transaction));

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);

        verify(dynamoDbClient).putItem(captor.capture());

        PutItemRequest savedRequest = captor.getValue();

        assertEquals("transactions", savedRequest.tableName());
        assertEquals(
                "attribute_not_exists(idempotencyKey)",
                savedRequest.conditionExpression());
        assertEquals(
                "test-key-001",
                savedRequest.item().get("idempotencyKey").s());
    }

    @Test
    void shouldReturnFalseWhenIdempotencyKeyAlreadyExists() {
        Transaction transaction = new Transaction(
                "test-key-002",
                "TXN-reference-002",
                new BigDecimal("200.00"),
                CurrencyCode.ZAR,
                Instant.parse("2026-11-13T10:00:00Z"));

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        assertFalse(repository.saveIfAbsent(transaction));
    }

    @Test
    void shouldRetrieveExistingTransaction() {
        Map<String, AttributeValue> item = Map.of(
                "idempotencyKey",
                AttributeValue.builder().s("test-key-003").build(),
                "transactionReference",
                AttributeValue.builder().s("TXN-reference-003").build(),
                "amount",
                AttributeValue.builder().n("300.00").build(),
                "currency",
                AttributeValue.builder().s("ZAR").build(),
                "createdAt",
                AttributeValue.builder()
                        .s("2026-11-13T10:00:00Z")
                        .build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        Optional<Transaction> result = repository.findByIdempotencyKey("test-key-003");

        assertTrue(result.isPresent());
        assertEquals(
                "TXN-reference-003",
                result.get().transactionReference());
        assertEquals(new BigDecimal("300.00"), result.get().amount());
        assertEquals(CurrencyCode.ZAR, result.get().currency());
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        Optional<Transaction> result = repository.findByIdempotencyKey("missing-key");

        assertTrue(result.isEmpty());
    }
}
