
package com.fintech.processor.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fintech.processor.model.TransactionRequest;
import com.fintech.processor.model.TransactionResponse;
import com.fintech.processor.model.TransactionStatus;
import com.fintech.processor.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionHandlerTest {

    private TransactionService transactionService;
    private TransactionHandler transactionHandler;
    private Context context;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        transactionHandler = new TransactionHandler(transactionService);
        context = mock(Context.class);
    }

    @Test
    void shouldPassRequestToTransactionService() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("150.00"),
                "ZAR",
                "test-key-001");

        TransactionResponse expectedResponse = new TransactionResponse(
                TransactionStatus.ACCEPTED,
                "TXN-test-reference",
                new BigDecimal("150.00"),
                "ZAR",
                "Transaction accepted successfully.");

        when(transactionService.process(request))
                .thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionHandler.handleRequest(request, context);

        assertEquals(expectedResponse, actualResponse);

        verify(transactionService).process(request);
    }

    @Test
    void shouldReturnServiceResponseWithoutChangingDuplicateStatus() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("200.00"),
                "ZAR",
                "test-key-002");

        TransactionResponse expectedResponse = new TransactionResponse(
                TransactionStatus.DUPLICATE,
                "TXN-existing-reference",
                new BigDecimal("200.00"),
                "ZAR",
                "Duplicate request. Existing transaction returned.");

        when(transactionService.process(request))
                .thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionHandler.handleRequest(request, context);

        assertEquals(TransactionStatus.DUPLICATE, actualResponse.status());
        assertEquals(
                "TXN-existing-reference",
                actualResponse.transactionReference());

        verify(transactionService).process(request);
    }

    @Test
    void shouldAllowNullLambdaContext() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "ZAR",
                "test-key-003");

        TransactionResponse expectedResponse = new TransactionResponse(
                TransactionStatus.ACCEPTED,
                "TXN-test-reference",
                new BigDecimal("100.00"),
                "ZAR",
                "Transaction accepted successfully.");

        when(transactionService.process(request))
                .thenReturn(expectedResponse);

        assertDoesNotThrow(
                () -> transactionHandler.handleRequest(request, null));
    }
}
