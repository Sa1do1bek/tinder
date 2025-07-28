package com.example.parking.service.impl;

import com.example.parking.entity.Car;
import com.example.parking.entity.UserBot;
import com.example.parking.repository.CarRepository;
import com.example.parking.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CarServiceImpl implements CarService {
    
    @Autowired
    private CarRepository carRepository;
    
    private static final Pattern LICENSE_PLATE_PATTERN = 
        Pattern.compile("^\\d{2}\\s[A-Z]\\s\\d{3}\\s[A-Z]{2}$");
    
    @Override
    public Car addCar(UserBot owner, String licensePlate, String brand, String model, String color) {
        Car car = new Car();
        car.setOwner(owner);
        car.setLicensePlate(formatLicensePlate(licensePlate));
        car.setBrand(brand);
        car.setModel(model);
        car.setColor(color);
        return carRepository.save(car);
    }
    
    @Override
    public Optional<Car> findByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlate(formatLicensePlate(licensePlate));
    }
    
    @Override
    public List<Car> findByOwner(Long ownerId) {
        return carRepository.findByOwnerChatId(ownerId);
    }
    
    @Override
    public boolean isLicensePlateExists(String licensePlate) {
        return carRepository.existsByLicensePlate(formatLicensePlate(licensePlate));
    }
    
    @Override
    public String formatLicensePlate(String input) {
        if (input == null) return null;
        
        String cleaned = input.replaceAll("\\s+", "").toUpperCase();
        
        if (cleaned.matches("^\\d{2}[A-Z]\\d{3}[A-Z]{2}$")) {
            return cleaned.substring(0, 2) + " " + 
                   cleaned.substring(2, 3) + " " + 
                   cleaned.substring(3, 6) + " " + 
                   cleaned.substring(6, 8);
        }
        
        return input.toUpperCase();
    }
    
    @Override
    public boolean isValidLicensePlate(String licensePlate) {
        if (licensePlate == null) return false;
        String formatted = formatLicensePlate(licensePlate);
        return LICENSE_PLATE_PATTERN.matcher(formatted).matches();
    }
}
