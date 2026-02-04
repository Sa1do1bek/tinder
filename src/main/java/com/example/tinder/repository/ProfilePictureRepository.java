package com.example.tinder.repository;

import com.example.tinder.entity.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    List<ProfilePicture> findAllByUserIdAndVisibilityTrue(Long userId);

    Optional<ProfilePicture> findByIdAndVisibilityTrue(Long id);
}

