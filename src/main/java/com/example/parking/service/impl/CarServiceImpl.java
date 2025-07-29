package com.example.parking.service.impl;

import com.example.parking.entity.Car;
import com.example.parking.entity.UserBot;
import com.example.parking.repository.CarRepository;
import com.example.parking.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarRepository carRepository;
    private static final Pattern PERSONAL_CAR_PATTERN =
            Pattern.compile("^\\d{2}\\s[A-Z]\\s\\d{3}\\s[A-Z]{2}$");

    private static final Pattern ORG_CAR_PATTERN_NEW =
            Pattern.compile("^\\d{2}\\s\\d{3}\\s[A-Z]{3}$");

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

        if (cleaned.length() == 8 && cleaned.matches("^\\d{2}[A-Z]\\d{3}[A-Z]{2}$")) {
            return cleaned.substring(0, 2) + " " +
                    cleaned.substring(2, 3) + " " +
                    cleaned.substring(3, 6) + " " +
                    cleaned.substring(6, 8);
        }

        if (cleaned.length() == 8 && cleaned.matches("^\\d{2}\\d{3}[A-Z]{3}$")) {
            return cleaned.substring(0, 2) + " " +
                    cleaned.substring(2, 5) + " " +
                    cleaned.substring(5, 8);
        }

        return input.toUpperCase();
    }

    @Override
    public boolean isValidLicensePlate(String licensePlate) {
        if (licensePlate == null) return false;
        String formatted = formatLicensePlate(licensePlate);
        return PERSONAL_CAR_PATTERN.matcher(formatted).matches() || LICENSE_PLATE_PATTERN.matcher(formatted).matches()
                || ORG_CAR_PATTERN_NEW.matcher(formatted).matches();
    }


    @Override
    public Optional<Car> findById(Long carId) {
        return carRepository.findById(carId);
    }

    @Override
    public Car updateCar(Long carId, String brand, String model, String color) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isPresent()) {
            Car car = carOpt.get();
            car.setBrand(brand != null ? brand.trim() : car.getBrand());
            car.setModel(model != null ? model.trim() : car.getModel());
            car.setColor(color != null ? color.trim() : car.getColor());
            return carRepository.save(car);
        }
        throw new RuntimeException("Car not found with id: " + carId);
    }

    @Override
    public boolean deleteCar(Long carId, Long ownerId) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isPresent()) {
            Car car = carOpt.get();
            if (car.getOwner().getChatId().equals(ownerId)) {
                carRepository.delete(car);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCarOwnedByUser(Long carId, Long userId) {
        Optional<Car> carOpt = carRepository.findById(carId);
        return carOpt.isPresent() && carOpt.get().getOwner().getChatId().equals(userId);
    }
}
