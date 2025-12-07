package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(unique = true, nullable = false)
    private String sku;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "backup_supplier_id")
    private Supplier backupSupplier;

    @Column(name = "supplier_price", nullable = false, precision = 10, scale = 2)
    private Double supplierPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private Double sellingPrice;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
