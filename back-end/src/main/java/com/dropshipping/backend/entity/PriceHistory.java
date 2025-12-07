package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Data
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "change_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal changePercentage;

    @Column(name = "change_reason", nullable = false, length = 100)
    private String changeReason;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy; // "system" or user identifier

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
