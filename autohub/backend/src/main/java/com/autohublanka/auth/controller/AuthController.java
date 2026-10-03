package com.autohublanka.auth.controller;

import com.autohublanka.auth.dto.AuthResponse;
import com.autohublanka.auth.dto.ForgotPasswordRequest;
import com.autohublanka.auth.dto.ForgotPasswordResponse;
import com.autohublanka.auth.dto.GoogleLoginRequest;
import com.autohublanka.auth.dto.LoginRequest;
import com.autohublanka.auth.dto.RegisterRequest;
import com.autohublanka.auth.dto.ResetPasswordRequest;
import com.autohublanka.auth.dto.VerifyOtpRequest;
import com.autohublanka.auth.service.AuthService;
import com.autohublanka.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http.getRemoteAddr());
    }

    /** Log in (or sign up as a customer) with the ID token from the "Continue with Google" button. */
    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request, HttpServletRequest http) {
        return authService.loginWithGoogle(request, http.getRemoteAddr());
    }

    @GetMapping("/google/config")
    public AuthService.GoogleConfig googleConfig() {
        return authService.googleConfig();
    }

    // ---------- forgot password (6-digit code sent by email) ----------

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.sendCode(request);
    }

    @PostMapping("/verify-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        passwordResetService.verify(request);
    }

    /** Sets the new password and logs the user in. */
    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest http) {
        return passwordResetService.reset(request, http.getRemoteAddr());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return authService.register(request, http.getRemoteAddr());
    }
}
