package com.autohublanka.sales.service;

import com.autohublanka.common.exception.NotFoundException;
import com.autohublanka.dashboard.service.SettingsService;
import com.autohublanka.sales.dto.InvoiceResponse;
import com.autohublanka.sales.entity.Invoice;
import com.autohublanka.sales.entity.Order;
import com.autohublanka.sales.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;
    private final SettingsService settingsService;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderService orderService, SettingsService settingsService) {
        this.invoiceRepository = invoiceRepository;
        this.orderService = orderService;
        this.settingsService = settingsService;
    }

    /** Customers see the invoice of their own orders; sales staff and the admin see all. */
    @Transactional(readOnly = true)
    public InvoiceResponse findByOrder(Long orderId) {
        Order order = orderService.getAccessibleOrder(orderId);
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("The invoice for order #" + orderId
                        + " is created automatically once the order is paid"));
        return InvoiceResponse.from(invoice, orderService.toResponse(order), settingsService.current().toPublic());
    }
}
