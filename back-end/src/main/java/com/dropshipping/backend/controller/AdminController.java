package com.dropshipping.backend.controller;

import com.dropshipping.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;

    // Spec alias: GET /api/admin/supplier-buy-list?date=YYYY-MM-DD
    @GetMapping("/supplier-buy-list")
    public List<OrderService.SupplierBuyGroup> supplierBuyList(@RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return orderService.buildSupplierBuyList(d);
    }

    // Spec alias: GET /api/admin/fulfillment-dashboard
    @GetMapping("/fulfillment-dashboard")
    public Map<String, Object> fulfillmentDashboard() {
        return orderService.buildFulfillmentDashboardSnapshot();
    }
}
