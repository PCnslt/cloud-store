package com.dropshipping.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_receipts")
@Data
public class SupplierReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "receipt_number", nullable = false, length = 100)
    private String receiptNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "USD";

    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ocr_data", columnDefinition = "jsonb")
    private JsonNode ocrData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
