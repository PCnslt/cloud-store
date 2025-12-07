package com.dropshipping.backend.service;

import com.dropshipping.backend.config.StripeProperties;
import com.dropshipping.backend.entity.Order;
import com.dropshipping.backend.entity.Payment;
import com.dropshipping.backend.enums.PaymentStatus;
import com.dropshipping.backend.repository.OrderRepository;
import com.dropshipping.backend.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.CustomerUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final StripeProperties stripeProperties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal STRIPE_PERCENT = new BigDecimal("0.029"); // 2.9%
    private static final BigDecimal STRIPE_FIXED = new BigDecimal("0.30");    // $0.30

    private void ensureStripeApiKey() {
        if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
            Stripe.apiKey = stripeProperties.getApiKey();
        }
    }

    /**
     * Records a payment intent against an order (idempotent by paymentIntentId).
     * This method does not confirm the payment with Stripe (assumes client-side confirmation).
     */
    public Payment processPayment(Long orderId, String paymentIntentId, String idempotencyKey) {
        ensureStripeApiKey();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        Optional<Payment> existing = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setStripePaymentIntentId(paymentIntentId);
        p.setAmount(order.getNetAmount());
        p.setCurrency("USD");
        p.setStatus(PaymentStatus.PENDING);
        p.setFeeAmount(BigDecimal.ZERO);
        p.setNetAmount(order.getNetAmount());
        p.setPaymentMethod("card");
        p.setPaymentGateway("STRIPE");
        return paymentRepository.save(p);
    }

    /**
     * Creates a refund in Stripe for a charge/paymentIntent and updates internal Payment record.
     */
    public Refund createRefund(Long paymentId, BigDecimal amount, String reason) throws StripeException {
        ensureStripeApiKey();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));

        RefundCreateParams.Builder builder = RefundCreateParams.builder();

        if (payment.getStripeChargeId() != null) {
            builder.setCharge(payment.getStripeChargeId());
        } else if (payment.getStripePaymentIntentId() != null) {
            builder.setPaymentIntent(payment.getStripePaymentIntentId());
        } else {
            throw new IllegalStateException("Payment has neither chargeId nor paymentIntentId");
        }

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            // Stripe expects amount in cents
            Long amountCents = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
            builder.setAmount(amountCents);
        }
        if (reason != null && !reason.isBlank()) {
            builder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
        }

        Refund refund = Refund.create(builder.build());

        // Update internal status
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return refund;
    }

    /**
     * Updates default payment method for a Stripe customer.
     */
    public void updatePaymentMethod(String stripeCustomerId, String paymentMethodId) throws StripeException {
        ensureStripeApiKey();

        // Attach PM to customer (if not attached)
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
        if (pm.getCustomer() == null) {
            pm.attach(PaymentMethodAttachParams.builder().setCustomer(stripeCustomerId).build());
        }

        // Set as default
        Customer customer = Customer.retrieve(stripeCustomerId);
        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                        .setDefaultPaymentMethod(paymentMethodId)
                        .build())
                .build();
        customer.update(params);
    }

    /**
     * Handle disputes (chargebacks) - skeleton for future expansion.
     */
    public void handleDispute(String chargeId) {
        // In a real implementation, collect evidence and submit via the Disputes API.
        // Here we just log and rely on a manual dashboard to track dispute status.
        log.warn("Dispute received for charge {}", chargeId);
    }

    /**
     * Records a Stripe charge received via webhook (idempotent on chargeId).
     * Attempts to link to an order via metadata orderId or via existing PaymentIntent record.
     */
    public Payment recordChargeFromEvent(Charge charge) {
        ensureStripeApiKey();
        String chargeId = charge.getId();
        Optional<Payment> existingByCharge = paymentRepository.findByStripeChargeId(chargeId);
        if (existingByCharge.isPresent()) {
            return existingByCharge.get();
        }

        // Try to resolve order
        Long orderId = null;
        Map<String, String> md = charge.getMetadata();
        if (md != null && md.containsKey("orderId")) {
            try {
                orderId = Long.parseLong(md.get("orderId"));
            } catch (NumberFormatException ignored) {}
        }
        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
        }

        // Fallback: find by payment_intent if we track it
        String piId = charge.getPaymentIntent();
        if (order == null && piId != null) {
            paymentRepository.findByStripePaymentIntentId(piId).ifPresent(p -> {
                // link order from existing payment
            });
        }

        if (order == null) {
            // As a last resort, do not fail; create unattached payment record (can be reconciled later)
            log.warn("Charge {} not linked to an order. Storing for reconciliation.", chargeId);
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setStripePaymentIntentId(piId);
        p.setStripeChargeId(chargeId);

        BigDecimal gross = toDollars(charge.getAmount());
        p.setAmount(gross);
        p.setCurrency(charge.getCurrency() != null ? charge.getCurrency().toUpperCase() : "USD");

        BigDecimal fee = estimateOrFetchStripeFee(charge);
        p.setFeeAmount(fee);
        p.setNetAmount(gross.subtract(fee).max(BigDecimal.ZERO));
        p.setPaymentMethod(charge.getPaymentMethodDetails() != null ? charge.getPaymentMethodDetails().getType() : "card");
        p.setPaymentGateway("STRIPE");
        p.setStatus(PaymentStatus.COMPLETED);

        return paymentRepository.save(p);
    }

    private BigDecimal toDollars(Long amountCents) {
        if (amountCents == null) return BigDecimal.ZERO;
        return new BigDecimal(amountCents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Try to fetch fee from balance transaction; if not available, estimate using standard rates.
     */
    private BigDecimal estimateOrFetchStripeFee(Charge charge) {
        try {
            String balanceTxId = charge.getBalanceTransaction();
            if (balanceTxId != null) {
                BalanceTransaction bt = BalanceTransaction.retrieve(balanceTxId);
                if (bt != null && bt.getFee() != null) {
                    return toDollars(bt.getFee().longValue());
                }
            }
        } catch (Exception ex) {
            log.debug("Could not retrieve Stripe balance transaction fee for charge {}: {}", charge.getId(), ex.getMessage());
        }
        BigDecimal amount = toDollars(charge.getAmount());
        return amount.multiply(STRIPE_PERCENT).add(STRIPE_FIXED).setScale(2, RoundingMode.HALF_UP);
    }
}
