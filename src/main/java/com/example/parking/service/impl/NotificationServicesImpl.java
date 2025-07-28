package com.example.parking.service.impl;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.keyboard.KeyboardFactory;
import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.UserBot;
import com.example.parking.service.NotificationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;

@Service("parkingNotificationService")
public class NotificationServicesImpl implements NotificationServices {
    
    @Autowired
    private KeyboardFactory keyboardFactory;
    
    @Override
    public void sendContactRequestNotification(ContactRequest request, ParkingTelegramBot bot) {
        String notificationText = String.format(
            "🚨 Sizning avtomobilingiz haqida murojaat!\n\n" +
            "🚗 Avtomobil: %s\n" +
            "📝 Sabab: %s\n" +
            "⏰ Vaqt: %s\n" +
            "👤 Murojaat qiluvchi: %s\n\n" +
            "Aloqa o'rnatish uchun quyidagi tugmalardan foydalaning:",
            request.getLicensePlate(),
            request.getReason().getDescription(),
            request.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            request.getSender().getFirstName()
        );
        
        sendMessage(bot, request.getReceiver().getChatId(), notificationText, 
                   keyboardFactory.getOwnerResponseKeyboard(request.getId()));
    }
    
    @Override
    public void sendPhoneNumberRequest(UserBot requester, UserBot owner, String licensePlate, ParkingTelegramBot bot) {
        String requestText = String.format(
            "📞 Telefon raqami so'rovi!\n\n" +
            "🚗 Avtomobil: %s\n" +
            "👤 So'rovchi: %s\n\n" +
            "Telefon raqamingizni ulashishni xohlaysizmi?",
            licensePlate,
            requester.getFirstName()
        );
        
        sendMessage(bot, owner.getChatId(), requestText, 
                   keyboardFactory.getPhoneShareKeyboard(requester.getChatId()));
    }
    
    @Override
    public void sendPhoneNumberResponse(UserBot owner, UserBot requester, String phoneNumber, ParkingTelegramBot bot) {
        String responseText = String.format(
            "📞 Telefon raqami ulashildi!\n\n" +
            "👤 %s\n" +
            "📱 %s\n\n" +
            "Aloqa o'rnatishingiz mumkin.",
            owner.getFirstName(),
            phoneNumber
        );
        
        sendMessage(bot, requester.getChatId(), responseText, null);
    }
    
    private void sendMessage(ParkingTelegramBot bot, Long chatId, String text, 
                           org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
