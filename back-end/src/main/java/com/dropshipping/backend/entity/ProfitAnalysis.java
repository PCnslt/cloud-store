package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "profit_analysis")
@Data
public class ProfitAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "supplier_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal supplierPrice;

    @Column(name = "stripe_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal stripeFee;

    @Column(name = "aws_cost", precision = 10, scale = 2)
    private BigDecimal awsCost = BigDecimal.ZERO;

    @Column(name = "transaction_cost", precision = 10, scale = 2)
    private BigDecimal transactionCost = BigDecimal.ZERO;

    @Column(name = "refund_reserve", precision = 10, scale = 2)
    private BigDecimal refundReserve = BigDecimal.ZERO;

    @Column(name = "shipping_insurance", precision = 10, scale = 2)
    private BigDecimal shippingInsurance = BigDecimal.ZERO;

    @Column(name = "net_profit", nullable = false, precision = 10, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "profit_margin", nullable = false, precision = 5, scale = 2)
    private BigDecimal profitMargin;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;
}
