package com.example.parking.repository;

import com.example.parking.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByLicensePlate(String licensePlate);
    List<Car> findByOwnerChatId(Long chatId);
    boolean existsByLicensePlate(String licensePlate);
}
