package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.ReconciliationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReconciliationAuditRepository extends JpaRepository<ReconciliationAudit, Long> {
    List<ReconciliationAudit> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
