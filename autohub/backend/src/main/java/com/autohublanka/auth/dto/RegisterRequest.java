package com.autohublanka.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public sign-up. Always creates a CUSTOMER account.
 * Phone number and delivery address are not asked here - they are collected at checkout.
 * Customers can only sign up with a Gmail address ({@link #GMAIL_REGEX}); the part before @ may contain
 * letters, digits, dots, +, _ and - (no dot at the start/end, no ".."). Staff and supplier accounts are created
 * by the admin / procurement and only need a valid format ({@link LoginRequest#EMAIL_REGEX}).
 */
public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name can have at most 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name can have at most 60 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Pattern(regexp = RegisterRequest.GMAIL_REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = RegisterRequest.GMAIL_MESSAGE)
        @Size(max = 120, message = "Email can have at most 120 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 64, message = "Password must be 6-64 characters")
        String password
) {
    public static final String GMAIL_REGEX = "^$|^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@gmail\\.com$";

    public static final String GMAIL_MESSAGE = "Please use a valid Gmail address (e.g. name@gmail.com)";
}
