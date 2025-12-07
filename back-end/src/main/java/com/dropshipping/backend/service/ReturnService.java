package com.dropshipping.backend.service;

import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.Payment;
import com.dropshipping.backend.entity.ReturnRequest;
import com.dropshipping.backend.repository.OrderItemRepository;
import com.dropshipping.backend.repository.PaymentRepository;
import com.dropshipping.backend.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessingService paymentProcessingService;

    @Transactional
    public ReturnRequest initiateReturn(Long orderItemId, String reason, BigDecimal requestedAmount) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("Order item not found: " + orderItemId));

        ReturnRequest rr = new ReturnRequest();
        rr.setOrderItem(item);
        rr.setReturnReason(reason);
        rr.setReturnStatus("REQUESTED");
        rr.setRefundAmount(requestedAmount);
        return returnRequestRepository.save(rr);
    }

    // Placeholder: generate a simple label token (in real world, integrate with carrier)
    public Map<String, String> generateReturnLabel(Long returnId) {
        ReturnRequest rr = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new NoSuchElementException("Return not found: " + returnId));
        // Demo label token
        String labelToken = "RET-LABEL-" + rr.getId() + "-" + System.currentTimeMillis();
        return Map.of(
                "returnId", String.valueOf(rr.getId()),
                "labelToken", labelToken,
                "instructions", "Print this token and include inside the package."
        );
    }

    @Transactional
    public ReturnRequest markReceived(Long returnId, String trackingNumber) {
        ReturnRequest rr = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new NoSuchElementException("Return not found: " + returnId));
        rr.setTrackingNumber(trackingNumber);
        rr.setReturnStatus("RECEIVED");
        rr.setReceivedAt(LocalDateTime.now());
        return returnRequestRepository.save(rr);
    }

    @Transactional
    public ReturnRequest processRefund(Long returnId, BigDecimal amount, String reason) {
        ReturnRequest rr = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new NoSuchElementException("Return not found: " + returnId));

        // Find an associated payment via order
        Long orderId = rr.getOrderItem().getOrder().getId();
        Payment payment = paymentRepository.findAllByOrder_Id(orderId).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No payment found for order " + orderId));

        try {
            paymentProcessingService.createRefund(payment.getId(), amount != null ? amount : rr.getRefundAmount(), reason);
            rr.setRefundStatus("COMPLETED");
            rr.setReturnStatus("REFUNDED");
            rr.setProcessedAt(LocalDateTime.now());
        } catch (Exception ex) {
            rr.setRefundStatus("FAILED");
        }
        return returnRequestRepository.save(rr);
    }

    public Map<String, Object> analytics() {
        long requested = returnRequestRepository.countByReturnStatus("REQUESTED");
        long received = returnRequestRepository.countByReturnStatus("RECEIVED");
        long refunded = returnRequestRepository.countByReturnStatus("REFUNDED");
        long rejected = returnRequestRepository.countByReturnStatus("REJECTED");
        return Map.of(
                "requested", requested,
                "received", received,
                "refunded", refunded,
                "rejected", rejected
        );
    }
}
