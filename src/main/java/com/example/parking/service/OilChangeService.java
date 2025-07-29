package com.example.parking.service;

import com.example.parking.entity.*;
import com.example.parking.repository.OilChangeInfoRepository;
import com.example.parking.repository.TempOilChangeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OilChangeService {

    @Autowired
    private OilChangeInfoRepository oilChangeInfoRepository;

    @Autowired
    private TempOilChangeDataRepository tempOilChangeDataRepository;

    public TempOilChangeData createOrUpdateTempData(Long chatId) {
        Optional<TempOilChangeData> existing = tempOilChangeDataRepository.findById(chatId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        TempOilChangeData tempData = TempOilChangeData.builder()
                .chatId(chatId)
                .build();
        return tempOilChangeDataRepository.save(tempData);
    }

    public Optional<TempOilChangeData> getTempData(Long chatId) {
        return tempOilChangeDataRepository.findById(chatId);
    }

    public void updateTempLastDate(Long chatId, LocalDate date) {
        Optional<TempOilChangeData> tempOpt = tempOilChangeDataRepository.findById(chatId);
        if (tempOpt.isPresent()) {
            TempOilChangeData temp = tempOpt.get();
            temp.setLastChangeDate(date);
            tempOilChangeDataRepository.save(temp);
        }
    }

    public void updateTempLastMileage(Long chatId, Integer mileage) {
        Optional<TempOilChangeData> tempOpt = tempOilChangeDataRepository.findById(chatId);
        if (tempOpt.isPresent()) {
            TempOilChangeData temp = tempOpt.get();
            temp.setLastChangeMileage(mileage);
            tempOilChangeDataRepository.save(temp);
        }
    }

    public void updateTempOilBrand(Long chatId, String brand) {
        Optional<TempOilChangeData> tempOpt = tempOilChangeDataRepository.findById(chatId);
        if (tempOpt.isPresent()) {
            TempOilChangeData temp = tempOpt.get();
            temp.setOilBrand(brand);
            tempOilChangeDataRepository.save(temp);
        }
    }

    public void updateTempRecommendedKm(Long chatId, Integer km) {
        Optional<TempOilChangeData> tempOpt = tempOilChangeDataRepository.findById(chatId);
        if (tempOpt.isPresent()) {
            TempOilChangeData temp = tempOpt.get();
            temp.setRecommendedKm(km);
            tempOilChangeDataRepository.save(temp);
        }
    }

    public void updateTempDailyDistance(Long chatId, Integer distance) {
        Optional<TempOilChangeData> tempOpt = tempOilChangeDataRepository.findById(chatId);
        if (tempOpt.isPresent()) {
            TempOilChangeData temp = tempOpt.get();
            temp.setDailyDistance(distance);
            tempOilChangeDataRepository.save(temp);
        }
    }

    public void deleteTempData(Long chatId) {
        tempOilChangeDataRepository.deleteById(chatId);
    }

    public boolean isValidDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(dateStr, formatter);
    }

    public boolean isValidMileage(String mileageStr) {
        try {
            int mileage = Integer.parseInt(mileageStr.replaceAll("[^0-9]", ""));
            return mileage > 0 && mileage < 1000000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int parseMileage(String mileageStr) {
        return Integer.parseInt(mileageStr.replaceAll("[^0-9]", ""));
    }

    public boolean isValidRecommendedKm(String kmStr) {
        try {
            int km = Integer.parseInt(kmStr.replaceAll("[^0-9]", ""));
            return km >= 1000 && km <= 50000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidDailyDistance(String distanceStr) {
        try {
            int distance = Integer.parseInt(distanceStr.replaceAll("[^0-9]", ""));
            return distance >= 1 && distance <= 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public LocalDate calculateNextChangeDate(LocalDate lastDate, int recommendedKm, int dailyDistance) {
        int daysToAdd = recommendedKm / dailyDistance;
        return lastDate.plusDays(daysToAdd);
    }

    public int calculateNextChangeMileage(int lastMileage, int recommendedKm) {
        return lastMileage + recommendedKm;
    }

    public OilChangeInfo saveOilChangeInfo(UserBot user, TempOilChangeData tempData) {
        LocalDate nextDate = calculateNextChangeDate(
                tempData.getLastChangeDate(),
                tempData.getRecommendedKm(),
                tempData.getDailyDistance()
        );
        
        int nextMileage = calculateNextChangeMileage(
                tempData.getLastChangeMileage(),
                tempData.getRecommendedKm()
        );

        OilChangeInfo oilInfo = OilChangeInfo.builder()
                .user(user)
                .lastChangeDate(tempData.getLastChangeDate())
                .lastChangeMileage(tempData.getLastChangeMileage())
                .oilBrand(tempData.getOilBrand())
                .recommendedKm(tempData.getRecommendedKm())
                .dailyDistance(tempData.getDailyDistance())
                .nextChangeDate(nextDate)
                .nextChangeMileage(nextMileage)
                .build();

        return oilChangeInfoRepository.save(oilInfo);
    }

    public List<OilChangeInfo> getUserOilChangeHistory(Long chatId) {
        return oilChangeInfoRepository.findByUserChatIdOrderByLastChangeDateDesc(chatId);
    }

    public Optional<OilChangeInfo> getLatestOilChangeInfo(Long chatId) {
        return oilChangeInfoRepository.findTopByUserChatIdOrderByLastChangeDateDesc(chatId);
    }

    public String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    public String formatMileage(int mileage) {
        return String.format("%,d km", mileage).replace(',', ' ');
    }

    public List<OilChangeInfo> getUpcomingOilChanges(LocalDate date) {
        return oilChangeInfoRepository.findByNextChangeDateLessThanEqual(date);
    }
}