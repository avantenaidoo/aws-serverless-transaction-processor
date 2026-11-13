
package com.fintech.processor.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fintech.processor.model.CurrencyCode;
import com.fintech.processor.model.Transaction;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class DynamoDbTransactionRepository implements TransactionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbTransactionRepository(
            DynamoDbClient dynamoDbClient,
            String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public boolean saveIfAbsent(Transaction transaction) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("idempotencyKey",
                AttributeValue.builder().s(transaction.idempotencyKey()).build());

        item.put("transactionReference",
                AttributeValue.builder().s(transaction.transactionReference()).build());

        item.put("amount",
                AttributeValue.builder().n(transaction.amount().toPlainString()).build());

        item.put("currency",
                AttributeValue.builder().s(transaction.currency().name()).build());

        item.put("createdAt",
                AttributeValue.builder().s(transaction.createdAt().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(idempotencyKey)")
                .build();

        try {
            dynamoDbClient.putItem(request);
            return true;
        } catch (ConditionalCheckFailedException exception) {
            return false;
        }
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {

        Map<String, AttributeValue> key = Map.of(
                "idempotencyKey",
                AttributeValue.builder().s(idempotencyKey).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .consistentRead(true)
                .build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }

        Transaction transaction = new Transaction(
                item.get("idempotencyKey").s(),
                item.get("transactionReference").s(),
                new BigDecimal(item.get("amount").n()),
                CurrencyCode.valueOf(item.get("currency").s()),
                Instant.parse(item.get("createdAt").s()));

        return Optional.of(transaction);
    }
}
