package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.Order;
import com.dropshipping.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findAllByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    // Paging helpers
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    // Review/metrics helpers
    List<Order> findAllByRequiresReviewTrue();
    List<Order> findAllByStatus(OrderStatus status);
    long countByStatus(OrderStatus status);
}
