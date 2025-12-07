package com.dropshipping.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_communications")
@Data
public class OrderCommunication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "communication_type", nullable = false, length = 50)
    private String communicationType;

    private String subject;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "sentiment_score", precision = 5, scale = 2)
    private Double sentimentScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments", columnDefinition = "jsonb")
    private JsonNode attachments;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
