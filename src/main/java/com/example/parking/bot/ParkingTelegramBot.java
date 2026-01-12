package com.example.parking.bot;

import com.example.parking.bot.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

//@Component
//public class ParkingTelegramBot extends TelegramLongPollingBot {
//
//    @Value("${telegram.bot.token}")
//    private String botToken;
//
//    @Value("${telegram.bot.username}")
//    private String botUsername;
//
//    @Autowired
//    private MessageHandler messageHandler;
//
//    @Override
//    public String getBotToken() {
//        return botToken;
//    }
//
//    @Override
//    public String getBotUsername() {
//        return botUsername;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        messageHandler.handleUpdate(update, this);
//    }
//}
