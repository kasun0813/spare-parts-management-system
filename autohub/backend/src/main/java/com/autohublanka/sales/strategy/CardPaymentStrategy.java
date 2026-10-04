package com.autohublanka.sales.strategy;

import com.autohublanka.common.exception.BusinessException;
import com.autohublanka.sales.dto.CardPaymentRequest;
import com.autohublanka.sales.entity.Order;
import com.autohublanka.sales.entity.Payment;
import com.autohublanka.sales.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * STRATEGY pattern - concrete strategy: online card payment (simulated gateway).
 * Checks the card and its expiry; the card number is never stored - only its last 4 digits.
 */
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(Order order, Payment payment, PaymentDetails details) {
        CardPaymentRequest card = details == null ? null : details.card();
        if (card == null) {
            throw new BusinessException("Card details are required");
        }
        YearMonth expiry = YearMonth.parse(card.expiry(), DateTimeFormatter.ofPattern("MM/yy"));
        if (expiry.isBefore(YearMonth.now())) {
            throw new BusinessException("This card has expired");
        }
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase());
        payment.setNote("Card ending " + card.cardNumber().substring(12));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        order.setPaymentStatus(PaymentStatus.PAID);
    }
}
