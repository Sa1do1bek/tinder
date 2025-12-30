package com.example.tinder.repository;

import com.example.tinder.entity.MediaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaMessageRepository extends JpaRepository<MediaMessage, Long> {
}
