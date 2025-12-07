package com.dropshipping.backend.enums;

public enum OrderStatus {
    PAYMENT_RECEIVED,
    SUPPLIER_ORDER_PLACED,
    SUPPLIER_CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    REQUIRES_MANUAL_REVIEW
}
