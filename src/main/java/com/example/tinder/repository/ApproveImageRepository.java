package com.example.tinder.repository;

import com.example.tinder.entity.ApproveImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproveImageRepository extends JpaRepository<ApproveImage, Long> {
}
