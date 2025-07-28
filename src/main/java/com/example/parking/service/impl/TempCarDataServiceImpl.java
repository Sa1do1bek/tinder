package com.example.parking.service.impl;

import com.example.parking.entity.TempCarData;
import com.example.parking.repository.TempCarDataRepository;
import com.example.parking.service.TempCarDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TempCarDataServiceImpl implements TempCarDataService {
    
    @Autowired
    private TempCarDataRepository tempCarDataRepository;
    
    @Override
    public TempCarData createOrUpdate(Long chatId) {
        TempCarData tempData = tempCarDataRepository.findById(chatId)
            .orElse(new TempCarData());
        tempData.setChatId(chatId);
        return tempCarDataRepository.save(tempData);
    }
    
    @Override
    public Optional<TempCarData> findByChatId(Long chatId) {
        return tempCarDataRepository.findById(chatId);
    }
    
    @Override
    public TempCarData updateLicensePlate(Long chatId, String licensePlate) {
        TempCarData tempData = tempCarDataRepository.findById(chatId)
            .orElse(new TempCarData());
        tempData.setChatId(chatId);
        tempData.setLicensePlate(licensePlate);
        return tempCarDataRepository.save(tempData);
    }
    
    @Override
    public TempCarData updateBrand(Long chatId, String brand) {
        TempCarData tempData = tempCarDataRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Temp data not found"));
        tempData.setBrand(brand);
        return tempCarDataRepository.save(tempData);
    }
    
    @Override
    public TempCarData updateModel(Long chatId, String model) {
        TempCarData tempData = tempCarDataRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Temp data not found"));
        tempData.setModel(model);
        return tempCarDataRepository.save(tempData);
    }
    
    @Override
    public TempCarData updateColor(Long chatId, String color) {
        TempCarData tempData = tempCarDataRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Temp data not found"));
        tempData.setColor(color);
        return tempCarDataRepository.save(tempData);
    }
    
    @Override
    public void delete(Long chatId) {
        tempCarDataRepository.deleteById(chatId);
    }
}
