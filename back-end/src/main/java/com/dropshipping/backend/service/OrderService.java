package com.dropshipping.backend.service;

import com.dropshipping.backend.config.OrderProperties;
import com.dropshipping.backend.dto.CreateOrderItemRequest;
import com.dropshipping.backend.dto.CreateOrderRequest;
import com.dropshipping.backend.entity.*;
import com.dropshipping.backend.enums.OrderStatus;
import com.dropshipping.backend.enums.ShipmentStatus;
import com.dropshipping.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final ReconciliationAuditRepository reconciliationAuditRepository;

    private final OrderProperties orderProperties;
    private final DuplicateOrderChecker duplicateOrderChecker;
    private final AuditTrailService auditTrailService;
    private final ProfitCalculatorService profitCalculatorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Page<Order> listOrders(Optional<OrderStatus> status, Pageable pageable) {
        if (status.isPresent()) {
            return orderRepository.findAllByStatus(status.get(), pageable);
        }
        return orderRepository.findAll(pageable);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        Order order = new Order();
        order.setOrderNumber(StringUtils.isNotBlank(req.getOrderNumber()) ? req.getOrderNumber() : generateOrderNumber());
        order.setCustomer(customer);
        order.setShippingAddress(req.getShippingAddress());
        order.setBillingAddress(req.getBillingAddress());
        order.setShippingAmount(nvl(req.getShippingAmount()));
        order.setTaxAmount(nvl(req.getTaxAmount()));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + itemReq.getProductId()));

            Supplier supplier = null;
            if (itemReq.getSupplierId() != null) {
                supplier = supplierRepository.findById(itemReq.getSupplierId())
                        .orElseThrow(() -> new NoSuchElementException("Supplier not found: " + itemReq.getSupplierId()));
            } else if (product.getSupplier() != null) {
                supplier = product.getSupplier();
            }

            // duplicate guard: customerId + productId + supplierId within 24 hours
            Long supplierIdForDup = supplier != null ? supplier.getId() : -1L;
            boolean duplicate = duplicateOrderChecker.isDuplicateAndLock(customer.getId(), product.getId(), supplierIdForDup);
            if (duplicate) {
                throw new IllegalStateException("Duplicate order detected for customer/product/supplier within 24 hours");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
            BigDecimal unitPrice = BigDecimal.valueOf(Optional.ofNullable(product.getSellingPrice()).orElse(0.0));
            item.setUnitPrice(unitPrice);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setTotalPrice(totalPrice);
            item.setSupplier(supplier);
            item.setShipmentStatus(ShipmentStatus.PENDING);
            items.add(item);

            itemsTotal = itemsTotal.add(totalPrice);
        }

        order.setTotalAmount(itemsTotal);
        order.setNetAmount(itemsTotal.add(order.getShippingAmount()).add(order.getTaxAmount()));

        // High-value order review logic
        BigDecimal threshold = orderProperties.getReview().getThreshold();
        if (threshold != null && order.getNetAmount().compareTo(threshold) > 0) {
            order.setRequiresReview(true);
            order.setReviewReason("High-value order exceeds threshold " + threshold);
            order.setStatus(OrderStatus.REQUIRES_MANUAL_REVIEW);
        } else {
            order.setRequiresReview(false);
            order.setStatus(OrderStatus.PAYMENT_RECEIVED);
        }

        // Compute and store cut-off time based on configured TZ and HH:mm
        try {
            String timeStr = orderProperties.getCutoff().getTime();
            String tz = orderProperties.getCutoff().getTimezone();
            LocalTime cutoffLocalTime = LocalTime.parse(timeStr);
            ZoneId zoneId = ZoneId.of(tz);
            ZonedDateTime todayCutoffZoned = ZonedDateTime.of(LocalDate.now(zoneId), cutoffLocalTime, zoneId);
            order.setCutOffTime(todayCutoffZoned.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        } catch (Exception ignored) {
        }

        order.setOrderItems(items);
        Order saved = orderRepository.save(order);

        // Profit calculation (baseline; more detailed computation after supplier price known)
        try {
            profitCalculatorService.computeAndSaveForOrder(saved.getId());
        } catch (Exception ignored) {}

        // Audit
        auditTrailService.log(
                "ORDER",
                saved.getId(),
                null,
                "ORDER_CREATED",
                null,
                toJsonNode(saved),
                null,
                null,
                null,
                null
        );

        return saved;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        OrderStatus before = order.getStatus();
        order.setStatus(status);
        Order saved = orderRepository.save(order);

        auditTrailService.log(
                "ORDER",
                saved.getId(),
                null,
                "ORDER_STATUS_UPDATED",
                toJsonNode(Map.of("status", before.name())),
                toJsonNode(Map.of("status", status.name())),
                null, null, null, null
        );

        return saved;
    }

    @Transactional
    public OrderItem updateOrderItemTracking(Long orderItemId, String trackingNumber) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("Order item not found"));

        String before = item.getTrackingNumber();
        item.setTrackingNumber(trackingNumber);
        item.setShipmentStatus(ShipmentStatus.SHIPPED);
        OrderItem saved = orderItemRepository.save(item);

        // If all items shipped, bump order status
        Order order = item.getOrder();
        boolean allShipped = order.getOrderItems().stream().allMatch(i -> i.getShipmentStatus() == ShipmentStatus.SHIPPED);
        if (allShipped && order.getStatus().ordinal() <= OrderStatus.SHIPPED.ordinal()) {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }

        auditTrailService.log(
                "ORDER_ITEM",
                saved.getId(),
                null,
                "ORDER_ITEM_TRACKING_UPDATED",
                toJsonNode(Map.of("trackingNumber", before)),
                toJsonNode(Map.of("trackingNumber", trackingNumber)),
                null, null, null, null
        );

        return saved;
    }

    public List<Order> getOrdersRequiringReview() {
        return orderRepository.findAllByRequiresReviewTrue();
    }

    @Transactional
    public Order reviewDecision(Long orderId, boolean approved, String reason) {
        Order order = getOrder(orderId);
        if (Boolean.FALSE.equals(order.getRequiresReview())) {
            return order;
        }
        OrderStatus before = order.getStatus();

        if (approved) {
            order.setRequiresReview(false);
            order.setReviewReason(reason);
            order.setStatus(OrderStatus.PAYMENT_RECEIVED);
        } else {
            order.setRequiresReview(false);
            order.setReviewReason(reason);
            order.setStatus(OrderStatus.CANCELLED);
        }
        Order saved = orderRepository.save(order);

        auditTrailService.log(
                "ORDER",
                saved.getId(),
                null,
                "ORDER_REVIEW_" + (approved ? "APPROVED" : "REJECTED"),
                toJsonNode(Map.of("status", before.name())),
                toJsonNode(Map.of("status", saved.getStatus().name(), "reviewReason", reason)),
                null, null, null, null
        );

        return saved;
    }

    // Supplier buy list DTOs and generator
    public record SupplierBuyItem(String orderNumber, String sku, String productName, int quantity, BigDecimal unitPrice,
                                  BigDecimal totalPrice, Object shippingAddress, boolean purchased) {}
    public record SupplierBuyGroup(Long supplierId, String supplierName, BigDecimal totalCost, long purchasedCount,
                                   long pendingCount, List<SupplierBuyItem> items) {}

    public List<SupplierBuyGroup> buildSupplierBuyList(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<OrderItem> items = orderItemRepository.findAllByCreatedAtBetween(start, end);
        Map<Supplier, List<OrderItem>> bySupplier = items.stream()
                .filter(oi -> oi.getSupplier() != null)
                .collect(Collectors.groupingBy(OrderItem::getSupplier));

        List<SupplierBuyGroup> result = new ArrayList<>();
        for (Map.Entry<Supplier, List<OrderItem>> e : bySupplier.entrySet()) {
            Supplier supplier = e.getKey();
            List<OrderItem> groupItems = e.getValue();

            List<SupplierBuyItem> buyItems = new ArrayList<>();
            BigDecimal totalCost = BigDecimal.ZERO;
            long purchased = 0;
            long pending = 0;

            for (OrderItem oi : groupItems) {
                boolean isPurchased = StringUtils.isNotBlank(oi.getSupplierConfirmationId());
                if (isPurchased) purchased++; else pending++;
                BigDecimal unitPrice = oi.getUnitPrice() != null ? oi.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(oi.getQuantity() != null ? oi.getQuantity() : 1));
                totalCost = totalCost.add(lineTotal);

                buyItems.add(new SupplierBuyItem(
                        oi.getOrder().getOrderNumber(),
                        oi.getProduct().getSku(),
                        oi.getProduct().getName(),
                        oi.getQuantity() != null ? oi.getQuantity() : 1,
                        unitPrice,
                        lineTotal,
                        oi.getOrder().getShippingAddress(),
                        isPurchased
                ));
            }

            result.add(new SupplierBuyGroup(
                    supplier.getId(),
                    supplier.getName(),
                    totalCost,
                    purchased,
                    pending,
                    buyItems
            ));
        }
        // Sort by supplier name for deterministic output
        result.sort(Comparator.comparing(SupplierBuyGroup::supplierName));
        return result;
    }

    // Fulfillment dashboard snapshot
    public Map<String, Object> buildFulfillmentDashboardSnapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("awaitingSupplierPurchase", orderItemRepository.findAllByShipmentStatus(ShipmentStatus.PENDING).size());
        m.put("requiresManualReview", orderRepository.findAllByRequiresReviewTrue().size());
        m.put("delayedShipments", orderItemRepository.findAllByShipmentStatus(ShipmentStatus.DELAYED).size());

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            statusCounts.put(s.name(), orderRepository.countByStatus(s));
        }
        m.put("orderStatusCounts", statusCounts);

        // Today's reconciliation audit entries
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime start = today.atStartOfDay();
        java.time.LocalDateTime end = today.plusDays(1).atStartOfDay();
        int reconciliationsToday = reconciliationAuditRepository.findAllByCreatedAtBetween(start, end).size();
        m.put("reconciliationsToday", reconciliationsToday);

        return m;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private com.fasterxml.jackson.databind.JsonNode toJsonNode(Object o) {
        try {
            return objectMapper.valueToTree(o);
        } catch (Exception e) {
            return null;
        }
    }
}
