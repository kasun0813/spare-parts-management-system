package com.autohublanka.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Login with email + password.
 * The email rules of the whole application are written here, next to the login and sign-up rules:
 * <ul>
 *   <li>{@link #EMAIL_REGEX} - strict email format used on every form (login, users, suppliers, settings,
 *       forgot password). The built-in @Email would also accept addresses such as "a@b".
 *       Accepts name@gmail.com, first.last@company.co.uk, name+tag@mail.lk; rejects a missing @ or domain dot,
 *       spaces, ".." and a dot at the start or end of the name.</li>
 *   <li>{@link RegisterRequest#GMAIL_REGEX} - customer sign-up only accepts Gmail addresses.</li>
 * </ul>
 * The same rules are used by the React forms (src/utils/validation.js). An empty value passes the pattern -
 * {@code @NotBlank} makes the email required.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = LoginRequest.EMAIL_REGEX, message = LoginRequest.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
    public static final String EMAIL_REGEX =
            "^$|^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?"
                    + "(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$";

    public static final String EMAIL_MESSAGE = "Enter a valid email address (e.g. name@gmail.com)";
}
