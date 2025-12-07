package com.dropshipping.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String orderNumber; // optional; if not provided, system will generate
    private Long customerId;
    private JsonNode shippingAddress;
    private JsonNode billingAddress;
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private List<CreateOrderItemRequest> items;
}
