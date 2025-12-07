package com.dropshipping.backend.service;

import com.dropshipping.backend.entity.Order;
import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.ProfitAnalysis;
import com.dropshipping.backend.repository.OrderRepository;
import com.dropshipping.backend.repository.ProfitAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ProfitCalculatorService {

    private static final BigDecimal STRIPE_PERCENT = new BigDecimal("0.029"); // 2.9%
    private static final BigDecimal STRIPE_FIXED = new BigDecimal("0.30");    // $0.30
    private static final BigDecimal REFUND_RESERVE_PERCENT = new BigDecimal("0.02"); // 2%
    private static final BigDecimal AWS_COST_PER_ORDER = BigDecimal.ZERO; // TODO: externalize to config if needed
    private static final BigDecimal SHIPPING_INSURANCE = BigDecimal.ZERO; // TODO: externalize to config if needed
    private static final BigDecimal TRANSACTION_COST = BigDecimal.ZERO;   // Non-Stripe variable costs if any

    private final OrderRepository orderRepository;
    private final ProfitAnalysisRepository profitAnalysisRepository;

    @Transactional
    public ProfitAnalysis computeAndSaveForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found " + orderId));

        // Selling price = sum of order item selling price (unit * qty)
        BigDecimal selling = BigDecimal.ZERO;
        BigDecimal supplier = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal unitSell = nz(item.getUnitPrice());
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            selling = selling.add(unitSell.multiply(BigDecimal.valueOf(qty)));

            // Supplier price from Product
            BigDecimal unitSupplier = BigDecimal.valueOf(item.getProduct().getSupplierPrice() != null ? item.getProduct().getSupplierPrice() : 0.0);
            supplier = supplier.add(unitSupplier.multiply(BigDecimal.valueOf(qty)));
        }

        // Stripe fee on the charged amount (netAmount includes shipping/tax)
        BigDecimal charged = nz(order.getNetAmount());
        BigDecimal stripeFee = charged.multiply(STRIPE_PERCENT).add(STRIPE_FIXED).setScale(2, RoundingMode.HALF_UP);

        BigDecimal awsCost = AWS_COST_PER_ORDER;
        BigDecimal transactionCost = TRANSACTION_COST;
        BigDecimal refundReserve = selling.multiply(REFUND_RESERVE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingInsurance = SHIPPING_INSURANCE;

        BigDecimal netProfit = selling.subtract(supplier)
                .subtract(stripeFee)
                .subtract(awsCost)
                .subtract(transactionCost)
                .subtract(refundReserve)
                .subtract(shippingInsurance)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitMargin = selling.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : netProfit.divide(selling, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        ProfitAnalysis pa = new ProfitAnalysis();
        pa.setOrder(order);
        pa.setSellingPrice(selling.setScale(2, RoundingMode.HALF_UP));
        pa.setSupplierPrice(supplier.setScale(2, RoundingMode.HALF_UP));
        pa.setStripeFee(stripeFee);
        pa.setAwsCost(awsCost);
        pa.setTransactionCost(transactionCost);
        pa.setRefundReserve(refundReserve);
        pa.setShippingInsurance(shippingInsurance);
        pa.setNetProfit(netProfit);
        pa.setProfitMargin(profitMargin);

        return profitAnalysisRepository.save(pa);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
