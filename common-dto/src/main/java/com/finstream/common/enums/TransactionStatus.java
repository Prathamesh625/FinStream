package com.finstream.common.enums;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REJECTED_INSUFFICIENT_FUNDS,
    REJECTED_FRAUD,
    REJECTED_DUPLICATE
}
