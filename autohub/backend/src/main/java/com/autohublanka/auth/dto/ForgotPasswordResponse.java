package com.autohublanka.auth.dto;

/**
 * Always the same answer, whether or not the email has an account - so the page cannot be used
 * to find out which emails are registered.
 */
public record ForgotPasswordResponse(String message, int expiresInMinutes, int resendAfterSeconds) {
}
