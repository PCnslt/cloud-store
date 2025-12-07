package com.dropshipping.backend.dto;

import lombok.Data;

@Data
public class CreateOrderItemRequest {
    private Long productId;
    private Integer quantity;
    private Long supplierId; // optional - can be auto-selected by system based on availability/pricing
}
