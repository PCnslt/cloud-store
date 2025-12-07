package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.PriceHistory;
import com.dropshipping.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findAllByProduct(Product product);
    List<PriceHistory> findAllByProduct_Id(Long productId);
    List<PriceHistory> findAllByEffectiveFromBetween(LocalDateTime start, LocalDateTime end);
}
