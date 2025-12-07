package com.dropshipping.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String website;

    @Column(name = "api_endpoint")
    private String apiEndpoint;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "performance_score", precision = 5, scale = 2)
    private Double performanceScore = 100.00;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_backup")
    private Boolean isBackup = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
