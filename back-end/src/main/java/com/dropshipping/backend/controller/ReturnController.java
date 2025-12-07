package com.dropshipping.backend.controller;

import com.dropshipping.backend.dto.ReturnInitiateRequest;
import com.dropshipping.backend.dto.ReturnRefundRequest;
import com.dropshipping.backend.dto.UpdateTrackingRequest;
import com.dropshipping.backend.entity.ReturnRequest;
import com.dropshipping.backend.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    // POST /api/returns/initiate
    @PostMapping("/initiate")
    public ResponseEntity<ReturnRequest> initiate(@RequestBody ReturnInitiateRequest req) {
        ReturnRequest created = returnService.initiateReturn(req.getOrderItemId(), req.getReason(), req.getRefundAmount());
        return ResponseEntity.created(URI.create("/api/returns/" + created.getId())).body(created);
    }

    // GET /api/returns/{id}/label
    @GetMapping("/{id}/label")
    public Map<String, String> label(@PathVariable Long id) {
        return returnService.generateReturnLabel(id);
    }

    // POST /api/returns/{id}/receive
    @PostMapping("/{id}/receive")
    public ReturnRequest markReceived(@PathVariable Long id, @RequestBody UpdateTrackingRequest req) {
        return returnService.markReceived(id, req.getTrackingNumber());
    }

    // POST /api/returns/{id}/refund
    @PostMapping("/{id}/refund")
    public ReturnRequest refund(@PathVariable Long id, @RequestBody ReturnRefundRequest req) {
        return returnService.processRefund(id, req.getAmount(), req.getReason());
    }

    // GET /api/returns/analytics
    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return returnService.analytics();
    }
}
