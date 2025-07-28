package com.example.parking.service;


import com.example.parking.entity.TempCarData;

import java.util.Optional;

public interface TempCarDataService {
    TempCarData createOrUpdate(Long chatId);
    Optional<TempCarData> findByChatId(Long chatId);
    TempCarData updateLicensePlate(Long chatId, String licensePlate);
    TempCarData updateBrand(Long chatId, String brand);
    TempCarData updateModel(Long chatId, String model);
    TempCarData updateColor(Long chatId, String color);
    void delete(Long chatId);
}
