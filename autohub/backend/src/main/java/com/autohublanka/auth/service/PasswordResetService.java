package com.autohublanka.auth.service;

import com.autohublanka.auth.dto.AuthResponse;
import com.autohublanka.auth.dto.ForgotPasswordRequest;
import com.autohublanka.auth.dto.ForgotPasswordResponse;
import com.autohublanka.auth.dto.ResetPasswordRequest;
import com.autohublanka.auth.dto.VerifyOtpRequest;
import com.autohublanka.auth.entity.PasswordResetOtp;
import com.autohublanka.auth.repository.PasswordResetOtpRepository;
import com.autohublanka.common.exception.BusinessException;
import com.autohublanka.dashboard.service.ActivityLogService;
import com.autohublanka.notification.service.EmailService;
import com.autohublanka.security.JwtService;
import com.autohublanka.user.dto.UserResponse;
import com.autohublanka.user.entity.User;
import com.autohublanka.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * "Forgot password" with a one-time code sent by email:
 * <ol>
 *   <li>{@link #sendCode} - a random 6-digit code (valid {@value #VALID_MINUTES} minutes) is emailed; only its hash is saved.
 *       A new code can be requested after {@value #RESEND_SECONDS} seconds.</li>
 *   <li>{@link #verify} - checks the code; after {@value #MAX_ATTEMPTS} wrong tries the code stops working.</li>
 *   <li>{@link #reset} - checks the code again, sets the new password, deletes the code and logs the user in.</li>
 * </ol>
 * Step 1 gives the same answer for unknown emails, so it cannot be used to find registered accounts.
 */
@Service
@Transactional(noRollbackFor = BusinessException.class) // a wrong code must still use up an attempt
public class PasswordResetService {

    public static final int VALID_MINUTES = 10;
    public static final int RESEND_SECONDS = 60;
    public static final int MAX_ATTEMPTS = 5;

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String INVALID = "This code is not valid or has expired. Please request a new code.";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final ActivityLogService activityLog;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetOtpRepository otpRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                JwtService jwtService,
                                ActivityLogService activityLog) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.activityLog = activityLog;
    }

    public ForgotPasswordResponse sendCode(ForgotPasswordRequest request) {
        ForgotPasswordResponse answer = new ForgotPasswordResponse(
                "If an account exists for this email, we have sent a 6-digit code to it.", VALID_MINUTES, RESEND_SECONDS);

        Optional<User> found = userRepository.findByEmailIgnoreCase(request.email().trim());
        if (found.isEmpty() || !found.get().isActive()) {
            return answer;
        }
        User user = found.get();
        PasswordResetOtp otp = otpRepository.findByUserId(user.getId()).orElseGet(PasswordResetOtp::new);
        LocalDateTime now = LocalDateTime.now();
        if (otp.getLastSentAt() != null && otp.getLastSentAt().plusSeconds(RESEND_SECONDS).isAfter(now)) {
            return answer; // asked again too quickly - the code already sent is still valid
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        otp.setUserId(user.getId());
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(now.plusMinutes(VALID_MINUTES));
        otp.setAttemptsLeft(MAX_ATTEMPTS);
        otp.setLastSentAt(now);
        otpRepository.save(otp);
        try {
            emailService.sendPasswordResetCode(user, code, VALID_MINUTES);
        } catch (MailException | IllegalStateException ex) {
            log.error("Could not send the password reset email to {}", user.getEmail(), ex);
            otpRepository.delete(otp);
            throw new BusinessException("We could not send the email right now. Please try again in a few minutes.");
        }
        return answer;
    }

    public void verify(VerifyOtpRequest request) {
        check(request.email(), request.code());
    }

    public AuthResponse reset(ResetPasswordRequest request, String ip) {
        User user = check(request.email(), request.code());
        if (!user.isActive()) {
            throw new DisabledException("This account has been deactivated. Please contact the administrator.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordSet(true);
        otpRepository.deleteByUserId(user.getId());
        activityLog.record(user, "PASSWORD_RESET", "Authentication", user.getId(), "Password reset with an email code", ip);
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    /** The user whose code this is - or a BusinessException (and one attempt used up). */
    private User check(String email, String code) {
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElseThrow(() -> new BusinessException(INVALID));
        PasswordResetOtp otp = otpRepository.findByUserId(user.getId()).orElseThrow(() -> new BusinessException(INVALID));
        if (otp.getExpiresAt().isBefore(LocalDateTime.now()) || otp.getAttemptsLeft() <= 0) {
            throw new BusinessException(INVALID);
        }
        if (!passwordEncoder.matches(code.trim(), otp.getCodeHash())) {
            otp.setAttemptsLeft(otp.getAttemptsLeft() - 1);
            otpRepository.save(otp);
            throw new BusinessException(otp.getAttemptsLeft() == 0
                    ? "Incorrect code. Too many wrong tries - please request a new code."
                    : "Incorrect code. " + otp.getAttemptsLeft() + " attempt(s) left.");
        }
        return user;
    }
}
