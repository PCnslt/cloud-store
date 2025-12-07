package com.dropshipping.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeProperties {
    /**
     * Secret API key for Stripe (do not hardcode, provide via env STRIPE_API_KEY).
     */
    private String apiKey;

    /**
     * Webhook signing secret (provide via env STRIPE_WEBHOOK_SECRET).
     */
    private String webhookSecret;
}
