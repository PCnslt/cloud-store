package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_audit")
@Data
public class ReconciliationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_charge_id", nullable = false, length = 255)
    private String stripeChargeId;

    @ManyToOne
    @JoinColumn(name = "supplier_receipt_id")
    private SupplierReceipt supplierReceipt;

    @Column(name = "customer_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal customerAmount;

    @Column(name = "supplier_amount", precision = 10, scale = 2)
    private BigDecimal supplierAmount;

    @Column(name = "discrepancy_amount", precision = 10, scale = 2)
    private BigDecimal discrepancyAmount;

    @Lob
    @Column(name = "discrepancy_reason")
    private String discrepancyReason;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
