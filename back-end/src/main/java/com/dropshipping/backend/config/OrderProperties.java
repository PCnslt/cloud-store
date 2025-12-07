package com.dropshipping.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "order")
@Getter
@Setter
public class OrderProperties {

    private Review review = new Review();
    private Cutoff cutoff = new Cutoff();

    @Getter
    @Setter
    public static class Review {
        /**
         * High-value order threshold; orders above this require manual review.
         */
        private BigDecimal threshold = new BigDecimal("500");
    }

    @Getter
    @Setter
    public static class Cutoff {
        /**
         * Daily cutoff time in HH:mm (24h) format, e.g. "14:00".
         */
        private String time = "14:00";
        /**
         * Timezone ID, e.g. "America/New_York".
         */
        private String timezone = "America/New_York";
    }
}
