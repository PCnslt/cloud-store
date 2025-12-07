package com.dropshipping.backend.controller;

import com.dropshipping.backend.entity.AuditTrail;
import com.dropshipping.backend.repository.AuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditTrailRepository auditTrailRepository;

    // GET /api/audit/order/{orderId}
    @GetMapping("/order/{orderId}")
    public List<AuditTrail> getOrderAudit(@PathVariable Long orderId) {
        return auditTrailRepository.findAllByEntityTypeAndEntityIdOrderByAuditTimestampDesc("ORDER", orderId);
    }
}
