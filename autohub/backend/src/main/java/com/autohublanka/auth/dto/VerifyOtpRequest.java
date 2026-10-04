package com.autohublanka.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Step 2: check the 6-digit code from the email (the code stays valid for step 3). */
public record VerifyOtpRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = LoginRequest.EMAIL_REGEX, message = LoginRequest.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Enter the code from the email")
        @Pattern(regexp = "^\\d{6}$", message = "The code has 6 digits")
        String code
) {
}
