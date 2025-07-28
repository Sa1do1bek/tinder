package com.example.parking.service;


import com.example.parking.entity.Car;
import com.example.parking.entity.UserBot;

import java.util.List;
import java.util.Optional;

public interface CarService {
    Car addCar(UserBot owner, String licensePlate, String brand, String model, String color);
    Optional<Car> findByLicensePlate(String licensePlate);
    List<Car> findByOwner(Long chatId);
    boolean isLicensePlateExists(String licensePlate);
    String formatLicensePlate(String input);
    boolean isValidLicensePlate(String licensePlate);
}
