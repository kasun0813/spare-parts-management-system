package com.autohublanka.support.factory;

import com.autohublanka.inventory.entity.MovementType;
import com.autohublanka.inventory.service.StockService;
import com.autohublanka.support.dto.RmaCompleteRequest;
import com.autohublanka.support.entity.Rma;

/** Concrete product: a new part is sent to the customer, so it leaves stock as a SALE movement. */
public class ReplacementResolution implements ClaimResolution {

    private final StockService stockService;

    public ReplacementResolution(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void resolve(Rma rma, RmaCompleteRequest request) {
        stockService.recordSystemMovement(rma.getOrderItem().getSparePart(), MovementType.SALE, rma.getQuantity(),
                "RMA-" + rma.getId(), "Replacement sent to customer");
    }
}
