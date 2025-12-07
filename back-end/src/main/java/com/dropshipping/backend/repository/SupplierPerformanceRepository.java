package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.SupplierPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, Long> {
    List<SupplierPerformance> findAllByPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(LocalDate start, LocalDate end);
    List<SupplierPerformance> findAllByOverallScoreLessThan(Double threshold);
}
