package com.autohublanka.support.dto;

import com.autohublanka.auth.dto.LoginRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** The Contact Us form. {@code website} is a hidden field people never fill in - bots do (spam trap). */
public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 80, message = "Name must be 2-80 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Pattern(regexp = LoginRequest.EMAIL_REGEX, message = LoginRequest.EMAIL_MESSAGE)
        @Size(max = 120)
        String email,

        @Pattern(regexp = "^$|^\\+?[0-9]{9,15}$", message = "Phone must contain 9-15 digits")
        String phone,

        @NotBlank(message = "Please choose a topic")
        @Pattern(regexp = "ORDER|PARTS|RETURNS|PAYMENT|OTHER", message = "Please choose a topic")
        String topic,

        @Pattern(regexp = "^$|^#?[0-9]{1,10}$", message = "Order number must be digits, e.g. 1024")
        String orderNumber,

        @NotBlank(message = "Subject is required")
        @Size(min = 3, max = 120, message = "Subject must be 3-120 characters")
        String subject,

        @NotBlank(message = "Message is required")
        @Size(min = 10, max = 2000, message = "Message must be 10-2000 characters")
        String message,

        String website
) {
}
