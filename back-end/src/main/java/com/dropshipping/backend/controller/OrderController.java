package com.dropshipping.backend.controller;

import com.dropshipping.backend.dto.CreateOrderRequest;
import com.dropshipping.backend.dto.UpdateOrderStatusRequest;
import com.dropshipping.backend.dto.UpdateTrackingRequest;
import com.dropshipping.backend.dto.ReviewDecisionRequest;
import com.dropshipping.backend.entity.Order;
import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.enums.OrderStatus;
import com.dropshipping.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // GET /api/orders?page=0&size=10&status=PAYMENT_RECEIVED
    @GetMapping
    public Page<Order> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.listOrders(Optional.ofNullable(status), pageable);
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest request) {
        Order created = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId())).body(created);
    }

    // PATCH /api/orders/{id}/status
    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest req) {
        return orderService.updateOrderStatus(id, req.getStatus());
    }

    // PATCH /api/orders/items/{orderItemId}/tracking
    @PatchMapping("/items/{orderItemId}/tracking")
    public OrderItem updateItemTracking(@PathVariable Long orderItemId, @RequestBody UpdateTrackingRequest req) {
        return orderService.updateOrderItemTracking(orderItemId, req.getTrackingNumber());
    }

    // GET /api/orders/admin/supplier-buy-list?date=YYYY-MM-DD
    @GetMapping("/admin/supplier-buy-list")
    public List<OrderService.SupplierBuyGroup> supplierBuyList(@RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return orderService.buildSupplierBuyList(d);
    }

    // GET /api/orders/admin/requires-review
    @GetMapping("/admin/requires-review")
    public List<Order> requiresReview() {
        return orderService.getOrdersRequiringReview();
    }

    // POST /api/orders/{orderId}/review { approved: boolean, reason?: string }
    @PostMapping("/{orderId}/review")
    public Order reviewDecision(@PathVariable Long orderId, @RequestBody ReviewDecisionRequest req) {
        return orderService.reviewDecision(orderId, req.isApproved(), req.getReason());
    }

    // GET /api/orders/admin/fulfillment-dashboard
    @GetMapping("/admin/fulfillment-dashboard")
    public Map<String, Object> fulfillmentDashboard() {
        return orderService.buildFulfillmentDashboardSnapshot();
    }
}
