package com.example.parking.bot.handler;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.handler.callback.CallbackHandler;
import com.example.parking.bot.handler.command.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;



@Component
public class MessageHandler {
    
    @Autowired
    private CommandHandler commandHandler;
    
    @Autowired
    private CallbackHandler callbackHandler;
    
    public void handleUpdate(Update update, ParkingTelegramBot bot) {
        try {
            if (update.hasMessage()) {
                if (update.getMessage().hasText()) {
                    commandHandler.handleTextMessage(update.getMessage(), bot);
                } else if (update.getMessage().hasContact()) {
                    commandHandler.handleContactMessage(update.getMessage(), bot);
                }
            } else if (update.hasCallbackQuery()) {
                callbackHandler.handleCallbackQuery(update.getCallbackQuery(), bot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
