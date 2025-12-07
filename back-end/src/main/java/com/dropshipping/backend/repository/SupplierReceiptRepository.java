package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.Supplier;
import com.dropshipping.backend.entity.SupplierReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SupplierReceiptRepository extends JpaRepository<SupplierReceipt, Long> {
    List<SupplierReceipt> findAllBySupplier(Supplier supplier);
    List<SupplierReceipt> findAllByReceiptDate(LocalDate date);
    List<SupplierReceipt> findAllByOrderItem(OrderItem orderItem);
    List<SupplierReceipt> findAllBySupplier_IdAndReceiptDateBetween(Long supplierId, LocalDate start, LocalDate end);
}
