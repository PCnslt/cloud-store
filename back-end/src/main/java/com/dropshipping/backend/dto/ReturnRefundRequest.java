package com.dropshipping.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnRefundRequest {
    private BigDecimal amount;
    private String reason;
}
