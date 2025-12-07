package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findAllByEntityTypeAndEntityIdOrderByAuditTimestampDesc(String entityType, Long entityId);
}
