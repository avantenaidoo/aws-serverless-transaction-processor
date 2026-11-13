
package com.fintech.processor.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fintech.processor.model.TransactionRequest;
import com.fintech.processor.model.TransactionResponse;
import com.fintech.processor.repository.DynamoDbTransactionRepository;
import com.fintech.processor.service.TransactionReferenceGenerator;
import com.fintech.processor.service.TransactionService;
import com.fintech.processor.validation.TransactionValidator;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class TransactionHandler
        implements RequestHandler<TransactionRequest, TransactionResponse> {

    private final TransactionService transactionService;

    // Constructor used by AWS Lambda.
    public TransactionHandler() {

        String tableName = System.getenv("TRANSACTIONS_TABLE");
        String region = System.getenv("AWS_REGION");

        if (tableName == null || tableName.isBlank()) {
            throw new IllegalStateException(
                    "TRANSACTIONS_TABLE environment variable is required.");
        }

        if (region == null || region.isBlank()) {
            throw new IllegalStateException(
                    "AWS_REGION environment variable is required.");
        }

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(region))
                .build();

        this.transactionService = new TransactionService(
                new DynamoDbTransactionRepository(dynamoDbClient, tableName),
                new TransactionValidator(),
                new TransactionReferenceGenerator());
    }

    // Constructor used for unit testing.
    TransactionHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public TransactionResponse handleRequest(
            TransactionRequest request,
            Context context) {
        return transactionService.process(request);
    }
}
