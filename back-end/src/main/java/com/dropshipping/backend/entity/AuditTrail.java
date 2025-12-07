package com.dropshipping.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_trails")
@Data
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_action", nullable = false, length = 100)
    private String userAction;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_change_history", columnDefinition = "jsonb")
    private JsonNode priceChangeHistory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_state", columnDefinition = "jsonb")
    private JsonNode beforeState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_state", columnDefinition = "jsonb")
    private JsonNode afterState;

    @CreationTimestamp
    @Column(name = "audit_timestamp")
    private LocalDateTime auditTimestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
