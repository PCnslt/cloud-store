package com.dropshipping.backend.controller;

import com.dropshipping.backend.config.StripeProperties;
import com.dropshipping.backend.service.PaymentProcessingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeProperties stripeProperties;
    private final PaymentProcessingService paymentProcessingService;

    @PostMapping(value = "/stripe", consumes = "application/json")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {
        String endpointSecret = stripeProperties.getWebhookSecret();
        if (endpointSecret == null || endpointSecret.isBlank()) {
            log.warn("Stripe webhook secret not configured");
            return ResponseEntity.badRequest().body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception ex) {
            log.warn("Stripe event construction failed: {}", ex.getMessage());
            return ResponseEntity.status(400).body("Invalid payload");
        }

        String type = event.getType();
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        try {
            switch (type) {
                case "charge.succeeded" -> {
                    if (dataObjectDeserializer.getObject().isPresent() && dataObjectDeserializer.getObject().get() instanceof Charge charge) {
                        paymentProcessingService.recordChargeFromEvent(charge);
                    }
                }
                case "charge.refunded" -> {
                    // In a more complete implementation, update internal refund status or create a refund record
                    log.info("Stripe event charge.refunded received");
                }
                case "charge.dispute.created" -> {
                    if (dataObjectDeserializer.getObject().isPresent() && dataObjectDeserializer.getObject().get() instanceof Charge charge) {
                        paymentProcessingService.handleDispute(charge.getId());
                    } else {
                        // If object is dispute itself, we could still log/handle by extracting charge id from event data
                        log.warn("Dispute created event received but could not deserialize charge object.");
                    }
                }
                default -> log.debug("Unhandled Stripe event type: {}", type);
            }
        } catch (Exception ex) {
            log.error("Error handling Stripe webhook event {}: {}", type, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body("Error handling event");
        }

        return ResponseEntity.ok("received");
    }
}
