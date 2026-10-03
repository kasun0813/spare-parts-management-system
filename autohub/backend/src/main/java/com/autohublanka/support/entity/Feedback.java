package com.autohublanka.support.entity;

import com.autohublanka.common.entity.BaseEntity;
import com.autohublanka.sales.entity.Order;
import com.autohublanka.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** General feedback about the shop / an order (not tied to one product). Shown straight away, no approval step. */
@Entity
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private int rating;

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
