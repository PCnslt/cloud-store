package com.dropshipping.backend.service;

import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.Payment;
import com.dropshipping.backend.entity.ReconciliationAudit;
import com.dropshipping.backend.entity.SupplierReceipt;
import com.dropshipping.backend.enums.PaymentStatus;
import com.dropshipping.backend.repository.OrderItemRepository;
import com.dropshipping.backend.repository.PaymentRepository;
import com.dropshipping.backend.repository.ReconciliationAuditRepository;
import com.dropshipping.backend.repository.SupplierReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Daily reconciliation: match Stripe charges with supplier receipts and flag discrepancies.
 * Runs at 11 PM EST (America/New_York).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionReconciliationService {

    private final PaymentRepository paymentRepository;
    private final SupplierReceiptRepository supplierReceiptRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReconciliationAuditRepository reconciliationAuditRepository;

    /**
     * Cron at 23:00 America/New_York timezone, every day.
     */
    @Scheduled(cron = "0 0 23 * * *", zone = "America/New_York")
    @Transactional
    public void nightlyReconciliation() {
        ZoneId tz = ZoneId.of("America/New_York");
        LocalDate targetDay = LocalDate.now(tz);
        reconcileForDate(targetDay);
    }

    /**
     * Public method for manual/backfill use if needed.
     */
    @Transactional
    public void reconcileForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // Get successful Stripe payments for the day
        List<Payment> payments = paymentRepository.findAllByStatusAndCreatedAtBetween(PaymentStatus.COMPLETED, start, end);

        for (Payment p : payments) {
            if (p.getOrder() == null) {
                // Unattached charge - store audit record with missing supplier amount
                saveAudit(p.getStripeChargeId(), p.getAmount(), null, "Unattached charge (no linked order)");
                continue;
            }

            // Sum supplier receipts for order items belonging to this order on the same date
            List<OrderItem> items = p.getOrder().getOrderItems();
            if (items == null || items.isEmpty()) {
                saveAudit(p.getStripeChargeId(), p.getAmount(), BigDecimal.ZERO, "No order items found");
                continue;
            }

            BigDecimal supplierTotalForDay = BigDecimal.ZERO;
            for (OrderItem item : items) {
                List<SupplierReceipt> receipts = supplierReceiptRepository.findAllByOrderItem(item);
                // If receipt date matches target date, include in sum
                BigDecimal lineReceipts = receipts.stream()
                        .filter(r -> r.getReceiptDate() != null && r.getReceiptDate().equals(date))
                        .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                supplierTotalForDay = supplierTotalForDay.add(lineReceipts);
            }

            BigDecimal customerAmount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
            BigDecimal discrepancy = customerAmount.subtract(supplierTotalForDay);
            String reason = discrepancy.abs().compareTo(new BigDecimal("0.01")) > 0
                    ? "Amount mismatch"
                    : "Matched";

            saveAudit(p.getStripeChargeId(), customerAmount, supplierTotalForDay, reason);
        }

        log.info("Reconciliation complete for date {}", date);
    }

    private void saveAudit(String stripeChargeId, BigDecimal customerAmount, BigDecimal supplierAmount, String reason) {
        ReconciliationAudit ra = new ReconciliationAudit();
        ra.setStripeChargeId(stripeChargeId != null ? stripeChargeId : "UNKNOWN");
        ra.setCustomerAmount(customerAmount != null ? customerAmount : BigDecimal.ZERO);
        ra.setSupplierAmount(supplierAmount);
        if (supplierAmount != null) {
            ra.setDiscrepancyAmount(customerAmount.subtract(supplierAmount));
        }
        ra.setDiscrepancyReason(reason);
        ra.setReconciledAt(LocalDateTime.now());
        reconciliationAuditRepository.save(ra);
    }
}
