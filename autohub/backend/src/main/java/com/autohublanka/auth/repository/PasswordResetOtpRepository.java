package com.autohublanka.auth.repository;

import com.autohublanka.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByUserId(Long userId);

    @Modifying
    @Query("delete from PasswordResetOtp o where o.userId = :userId")
    void deleteByUserId(Long userId);
}
