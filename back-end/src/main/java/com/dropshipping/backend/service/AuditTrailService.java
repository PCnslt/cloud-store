package com.dropshipping.backend.service;

import com.dropshipping.backend.entity.AuditTrail;
import com.dropshipping.backend.repository.AuditTrailRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    public void log(String entityType,
                    Long entityId,
                    Long userId,
                    String userAction,
                    JsonNode beforeState,
                    JsonNode afterState,
                    BigDecimal originalPrice,
                    BigDecimal sellingPrice,
                    BigDecimal profitMargin,
                    String ipAddress) {
        AuditTrail audit = new AuditTrail();
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setUserId(userId);
        audit.setUserAction(userAction);
        audit.setBeforeState(beforeState);
        audit.setAfterState(afterState);
        audit.setOriginalPrice(originalPrice);
        audit.setSellingPrice(sellingPrice);
        audit.setProfitMargin(profitMargin);
        audit.setIpAddress(ipAddress);
        auditTrailRepository.save(audit);
    }
}
