package com.dropshipping.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnInitiateRequest {
    private Long orderItemId;
    private String reason;
    private BigDecimal refundAmount; // optional; can be set later during refund processing
}
