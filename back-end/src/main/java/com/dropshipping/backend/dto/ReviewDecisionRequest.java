package com.dropshipping.backend.dto;

import lombok.Data;

@Data
public class ReviewDecisionRequest {
    private boolean approved;
    private String reason;
}
