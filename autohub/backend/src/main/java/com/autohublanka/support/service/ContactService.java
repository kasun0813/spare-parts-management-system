package com.autohublanka.support.service;

import com.autohublanka.common.exception.BusinessException;
import com.autohublanka.common.util.Text;
import com.autohublanka.dashboard.dto.AppSettings;
import com.autohublanka.dashboard.service.SettingsService;
import com.autohublanka.notification.service.EmailService;
import com.autohublanka.notification.service.NotificationService;
import com.autohublanka.support.dto.ContactRequest;
import com.autohublanka.support.dto.ContactResponse;
import com.autohublanka.support.singleton.ContentFilter;
import com.autohublanka.user.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contact Us: the message is emailed to the shop's own Gmail account - the one in spring.mail.username, the same
 * account that sends the "Forgot password" codes (reply goes straight to the customer),
 * the customer gets a confirmation copy, and sales staff get an in-app notification.
 * Spam protection: a hidden "website" field and at most {@value #MAX_PER_WINDOW} messages per 10 minutes per address.
 */
@Service
public class ContactService {

    static final int MAX_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final Logger log = LoggerFactory.getLogger(ContactService.class);
    private static final Map<String, String> TOPICS = Map.of(
            "ORDER", "My order", "PARTS", "Spare part enquiry", "RETURNS", "Returns & warranty",
            "PAYMENT", "Payment", "OTHER", "Other");

    private final EmailService emailService;
    private final SettingsService settingsService;
    private final NotificationService notifications;
    private final String contactTo;
    private final Map<String, Deque<Instant>> recent = new ConcurrentHashMap<>();

    public ContactService(EmailService emailService, SettingsService settingsService, NotificationService notifications,
                          @Value("${app.contact.to:}") String contactTo) {
        this.contactTo = contactTo == null ? "" : contactTo.trim();
        this.emailService = emailService;
        this.settingsService = settingsService;
        this.notifications = notifications;
    }

    public ContactResponse send(ContactRequest request, String ip) {
        ContactResponse thanks = new ContactResponse("Thank you, " + request.name().trim().split("\\s+")[0]
                + "! We have received your message and will reply to " + request.email().trim() + " within one working day.");
        if (Text.clean(request.website()) != null) {
            log.info("Contact form spam trap filled from {} - ignored", ip);
            return thanks;
        }
        limit(ip);
        ContentFilter.getInstance().requireClean(request.subject(), request.message());

        AppSettings store = settingsService.current();
        String name = request.name().trim();
        String email = request.email().trim().toLowerCase();
        String topic = TOPICS.get(request.topic());
        String order = Text.clean(request.orderNumber()) == null ? null : request.orderNumber().replace("#", "").trim();
        String phone = Text.clean(request.phone());
        String subject = request.subject().trim();
        String message = request.message().trim();

        String details = "Name: " + name + "\nEmail: " + email + (phone == null ? "" : "\nPhone: " + phone)
                + "\nTopic: " + topic + (order == null ? "" : "\nOrder: #" + order);
        String toShopText = "New message from the AutoHub Lanka website\n\n" + details + "\n\nSubject: " + subject + "\n\n" + message
                + "\n\n(Reply to this email to answer the customer.)";
        String toShopHtml = EmailService.layout("""
                <p><b>New message from the website contact form</b></p>
                <table style="font-size:14px;border-collapse:collapse">
                  <tr><td style="color:#666;padding:2px 12px 2px 0">Name</td><td>%s</td></tr>
                  <tr><td style="color:#666;padding:2px 12px 2px 0">Email</td><td><a href="mailto:%s">%s</a></td></tr>
                  %s
                  <tr><td style="color:#666;padding:2px 12px 2px 0">Topic</td><td>%s</td></tr>
                  %s
                </table>
                <p style="margin-top:16px"><b>%s</b></p>
                <p style="background:#f4f4f5;padding:12px 14px;border-radius:8px">%s</p>
                <p style="color:#666;font-size:13px">Reply to this email to answer %s directly.</p>
                """.formatted(EmailService.escape(name), EmailService.escape(email), EmailService.escape(email),
                phone == null ? "" : "<tr><td style=\"color:#666;padding:2px 12px 2px 0\">Phone</td><td>" + EmailService.escape(phone) + "</td></tr>",
                EmailService.escape(topic),
                order == null ? "" : "<tr><td style=\"color:#666;padding:2px 12px 2px 0\">Order</td><td>#" + EmailService.escape(order) + "</td></tr>",
                EmailService.escape(subject), EmailService.escape(message), EmailService.escape(name)));

        try {
            emailService.send(shopInbox(), email, "[Contact] " + topic + ": " + subject, toShopText, toShopHtml);
        } catch (MailException | IllegalStateException ex) {
            log.error("Could not send the contact form email", ex);
            throw new BusinessException("Sorry, your message could not be sent right now. Please try again later or call us on "
                    + store.storePhone() + ".");
        }

        // a copy for the customer - not essential, so a failure here does not fail the request
        try {
            String copyText = "Hello " + name + ",\n\nThank you for contacting " + store.storeName() + ". We have received your message "
                    + "and will reply within one working day.\n\nYour message:\n" + subject + "\n" + message + "\n\n" + store.storeName()
                    + "\n" + store.storePhone();
            String copyHtml = EmailService.layout("""
                    <p>Hello %s,</p>
                    <p>Thank you for contacting %s. We have received your message and will reply within one working day.</p>
                    <p style="color:#666;font-size:13px;margin-bottom:4px">Your message</p>
                    <p style="background:#f4f4f5;padding:12px 14px;border-radius:8px;margin-top:0"><b>%s</b><br>%s</p>
                    <p>Need help sooner? Call us on <b>%s</b>.</p>
                    """.formatted(EmailService.escape(name), EmailService.escape(store.storeName()), EmailService.escape(subject),
                    EmailService.escape(message), EmailService.escape(store.storePhone())));
            emailService.send(email, shopInbox(), "We received your message - " + store.storeName(), copyText, copyHtml);
        } catch (MailException | IllegalStateException ex) {
            log.warn("Could not send the contact confirmation to {}: {}", email, ex.getMessage());
        }

        notifications.notifyRole(Role.SALES_STAFF, "Contact message: " + subject,
                name + " (" + email + ") · " + topic + " - reply by email.", null);
        return thanks;
    }

    /** Contact messages go to the shop's Gmail account (spring.mail.username); before it is set up, to the store email. */
    private String shopInbox() {
        String account = emailService.accountAddress();
        return account.isEmpty() ? settingsService.current().storeEmail() : account;
    }

    private void limit(String ip) {
        Instant now = Instant.now();
        Deque<Instant> times = recent.computeIfAbsent(ip == null ? "?" : ip, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
                times.pollFirst();
            }
            if (times.size() >= MAX_PER_WINDOW) {
                throw new BusinessException("You have sent several messages in a short time. Please wait a few minutes and try again.");
            }
            times.addLast(now);
        }
    }
}
