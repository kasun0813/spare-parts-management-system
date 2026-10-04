package com.autohublanka.procurement.observer;

import com.autohublanka.procurement.entity.PurchaseOrder;
import com.autohublanka.procurement.entity.PurchaseOrderStatus;

/** OBSERVER pattern - the Observer: told when a purchase order is created (previousStatus null) or changes status. */
public interface PurchaseOrderObserver {

    void update(PurchaseOrder order, PurchaseOrderStatus previousStatus);
}
