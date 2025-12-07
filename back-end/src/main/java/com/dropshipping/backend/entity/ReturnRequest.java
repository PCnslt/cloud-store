package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
@Data
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Lob
    @Column(name = "return_reason", nullable = false)
    private String returnReason;

    @Column(name = "return_status", length = 50)
    private String returnStatus = "REQUESTED"; // REQUESTED, APPROVED, IN_TRANSIT, RECEIVED, REFUNDED, REJECTED

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_status", length = 50)
    private String refundStatus; // PENDING, COMPLETED, FAILED

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
