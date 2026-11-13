
package com.fintech.processor.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TransactionReferenceGeneratorTest {

    private final TransactionReferenceGenerator generator = new TransactionReferenceGenerator();

    @Test
    void shouldGenerateReferenceWithTxnPrefix() {
        String reference = generator.generate();

        assertTrue(reference.startsWith("TXN-"));
    }

    @Test
    void shouldGenerateUniqueReferences() {
        String firstReference = generator.generate();
        String secondReference = generator.generate();

        assertNotEquals(firstReference, secondReference);
    }
}
