package com.autohublanka.support.factory;

import com.autohublanka.common.exception.BusinessException;
import com.autohublanka.inventory.entity.MovementType;
import com.autohublanka.inventory.service.StockService;
import com.autohublanka.sales.entity.OrderItem;
import com.autohublanka.support.dto.RmaCompleteRequest;
import com.autohublanka.support.entity.Rma;
import com.autohublanka.support.entity.RmaType;

import java.math.BigDecimal;

/** Concrete product: money back. The refund can't exceed the price paid; a returned part goes back into stock. */
public class RefundResolution implements ClaimResolution {

    private final StockService stockService;

    public RefundResolution(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void resolve(Rma rma, RmaCompleteRequest request) {
        OrderItem item = rma.getOrderItem();
        BigDecimal max = item.getUnitPrice().multiply(BigDecimal.valueOf(rma.getQuantity()));
        if (request.refundAmount() == null) {
            throw new BusinessException("Refund amount is required");
        }
        if (request.refundAmount().compareTo(max) > 0) {
            throw new BusinessException("Refund cannot be more than Rs. " + max + " (price paid for " + rma.getQuantity() + " unit(s))");
        }
        rma.setRefundAmount(request.refundAmount());
        if (rma.getType() == RmaType.RETURN) {
            stockService.recordSystemMovement(item.getSparePart(), MovementType.RETURN, rma.getQuantity(),
                    "RMA-" + rma.getId(), "Returned by customer");
        }
    }
}
