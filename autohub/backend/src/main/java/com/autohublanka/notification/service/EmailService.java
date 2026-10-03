package com.autohublanka.notification.service;

import com.autohublanka.user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Sends emails through the SMTP account in application.properties (spring.mail.*, e.g. a Gmail address with
 * an App Password). Used by "Forgot password" and the Contact Us form.
 * While no account is configured nothing is sent - the email is written to the backend console instead,
 * so both features can still be tried out during development.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String username;
    private final String fromName;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.username:}") String username,
                        @Value("${app.mail.from-name:AutoHub Lanka}") String fromName) {
        this.mailSender = mailSender;
        this.username = username == null ? "" : username.trim();
        this.fromName = fromName;
    }

    /** The Gmail account in spring.mail.username - emails are sent from it, and Contact Us messages are sent to it. */
    public String accountAddress() {
        return username;
    }

    public boolean isConfigured() {
        return !username.isEmpty() && mailSender.getIfAvailable() != null;
    }

    /**
     * Sends one email (plain text + HTML). {@code replyTo} may be null.
     * @return false when email is not configured (the email was only written to the console)
     * @throws MailException / IllegalStateException when the email could not be sent
     */
    public boolean send(String to, String replyTo, String subject, String text, String html) {
        if (!isConfigured()) {
            log.warn("Email is not configured (spring.mail.username) - not sent.\nTo: {}\nSubject: {}\n{}", to, subject, text);
            return false;
        }
        try {
            JavaMailSender sender = mailSender.getObject();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username, fromName);
            helper.setTo(to);
            if (replyTo != null) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(subject);
            helper.setText(text, html);
            sender.send(message);
            log.info("Email \"{}\" sent to {}", subject, to);
            return true;
        } catch (MessagingException | UnsupportedEncodingException ex) {
            throw new IllegalStateException("Could not build the email: " + ex.getMessage(), ex);
        }
    }

    public void sendPasswordResetCode(User user, String code, int validMinutes) {
        if (!isConfigured()) {
            log.warn("Email is not configured (spring.mail.username) - PASSWORD RESET CODE for {}: {}", user.getEmail(), code);
            return;
        }
        String text = "Hello " + user.getFirstName() + ",\n\n"
                + "Your AutoHub Lanka password reset code is: " + code + "\n\n"
                + "It is valid for " + validMinutes + " minutes. If you did not ask to reset your password, ignore this email - "
                + "your password stays the same.\n\nAutoHub Lanka";
        String html = layout("""
                <p>Hello %s,</p>
                <p>Use this code to reset your password:</p>
                <p style="font-size:32px;font-weight:bold;letter-spacing:8px;background:#f4f4f5;padding:14px 0;text-align:center;border-radius:8px">%s</p>
                <p>The code is valid for <b>%d minutes</b>.</p>
                <p style="color:#666;font-size:13px">If you did not ask to reset your password, ignore this email - your password stays the same.</p>
                """.formatted(escape(user.getFirstName()), code, validMinutes));
        send(user.getEmail(), null, "Your AutoHub Lanka password reset code: " + code, text, html);
    }

    /** The AutoHub header + footer around an email body. */
    public static String layout(String body) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#111;line-height:1.5">
                  <h2 style="margin:0 0 12px">AUTO<span style="color:#e3161f">HUB</span> Lanka</h2>
                  %s
                  <p style="color:#999;font-size:12px;margin-top:24px">AutoHub Lanka - genuine spare parts, delivered island-wide.</p>
                </div>
                """.formatted(body);
    }

    public static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("\n", "<br>");
    }
}
