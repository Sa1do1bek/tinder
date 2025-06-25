package com.example.tinder.repository;

import com.example.tinder.entity.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    List<ProfilePicture> findAllByUserId(Long userId);
}

