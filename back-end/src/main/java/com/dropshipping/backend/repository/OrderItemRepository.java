package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.Supplier;
import com.dropshipping.backend.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<OrderItem> findAllBySupplier(Supplier supplier);
    List<OrderItem> findAllByShipmentStatus(ShipmentStatus status);
}
