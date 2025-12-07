package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.ProfitAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfitAnalysisRepository extends JpaRepository<ProfitAnalysis, Long> {
    List<ProfitAnalysis> findAllByOrder_Id(Long orderId);
}
