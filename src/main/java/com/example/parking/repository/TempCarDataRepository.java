package com.example.parking.repository;

import com.example.parking.entity.TempCarData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempCarDataRepository extends JpaRepository<TempCarData, Long> {
    Optional<TempCarData> findByChatId(Long chatId);
}
