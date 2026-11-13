package com.fintech.processor.service;

import java.util.UUID;

public class TransactionReferenceGenerator {

    public String generate() {
        return "TXN-" + UUID.randomUUID();
    }
    
}
