package com.autohublanka.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Step 3: choose the new password (the code is checked again and then used up). */
public record ResetPasswordRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = LoginRequest.EMAIL_REGEX, message = LoginRequest.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Enter the code from the email")
        @Pattern(regexp = "^\\d{6}$", message = "The code has 6 digits")
        String code,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 64, message = "Password must be 6-64 characters")
        String newPassword
) {
}
