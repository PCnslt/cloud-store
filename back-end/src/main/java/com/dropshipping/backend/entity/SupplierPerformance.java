package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_performance")
@Data
public class SupplierPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "on_time_delivery_rate", precision = 5, scale = 2)
    private Double onTimeDeliveryRate = 0.0;

    @Column(name = "order_accuracy_rate", precision = 5, scale = 2)
    private Double orderAccuracyRate = 0.0;

    @Column(name = "communication_score", precision = 5, scale = 2)
    private Double communicationScore = 0.0;

    @Column(name = "price_competitiveness", precision = 5, scale = 2)
    private Double priceCompetitiveness = 0.0;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private Double overallScore = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
