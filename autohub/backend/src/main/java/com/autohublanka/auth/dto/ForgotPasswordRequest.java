package com.autohublanka.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Step 1 of "Forgot password": the email to send the code to. */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = LoginRequest.EMAIL_REGEX, message = LoginRequest.EMAIL_MESSAGE)
        String email
) {
}
