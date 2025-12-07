package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findAllByReturnStatus(String status);
    long countByReturnStatus(String status);
}
