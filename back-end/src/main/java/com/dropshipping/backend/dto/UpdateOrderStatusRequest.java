package com.dropshipping.backend.dto;

import com.dropshipping.backend.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
