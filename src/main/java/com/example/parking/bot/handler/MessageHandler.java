package com.example.parking.bot.handler;

import com.example.parking.bot.handler.callback.CallbackHandler;
import com.example.parking.bot.handler.command.CommandHandler;
import com.example.tinder.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;



@Component
@RequiredArgsConstructor
public class MessageHandler {
    
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    
    public void handleUpdate(Update update, com.example.tinder.bot.TelegramBot bot) {
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
