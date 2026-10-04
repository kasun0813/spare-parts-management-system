package com.autohublanka.sales.repository;

import com.autohublanka.sales.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    long countBySparePartId(Long sparePartId);
}
