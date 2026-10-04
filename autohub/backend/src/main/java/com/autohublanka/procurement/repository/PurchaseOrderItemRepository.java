package com.autohublanka.procurement.repository;

import com.autohublanka.procurement.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    long countBySparePartId(Long sparePartId);
}
