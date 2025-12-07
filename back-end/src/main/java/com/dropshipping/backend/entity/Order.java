package com.dropshipping.backend.entity;

import com.dropshipping.backend.enums.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PAYMENT_RECEIVED;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "requires_review")
    private Boolean requiresReview = false;

    @Column(name = "review_reason")
    private String reviewReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false, columnDefinition = "jsonb")
    private JsonNode shippingAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_address", nullable = false, columnDefinition = "jsonb")
    private JsonNode billingAddress;

    @Column(name = "estimated_delivery_start")
    private LocalDate estimatedDeliveryStart;

    @Column(name = "estimated_delivery_end")
    private LocalDate estimatedDeliveryEnd;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "cut_off_time")
    private LocalDateTime cutOffTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
