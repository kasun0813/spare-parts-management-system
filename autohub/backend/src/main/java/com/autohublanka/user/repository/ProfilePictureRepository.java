package com.autohublanka.user.repository;

import com.autohublanka.user.entity.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    Optional<ProfilePicture> findByUserId(Long userId);

    @Modifying
    @Query("delete from ProfilePicture p where p.userId = :userId")
    void deleteByUserId(Long userId);
}
