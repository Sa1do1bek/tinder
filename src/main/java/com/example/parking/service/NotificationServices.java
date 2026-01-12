package com.example.parking.service;


import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.UserBot;
import com.example.tinder.bot.TelegramBot;

public interface NotificationServices {
    void sendContactRequestNotification(ContactRequest request, TelegramBot bot);
    void sendPhoneNumberRequest(UserBot requester, UserBot owner, String licensePlate, TelegramBot bot);
    void sendPhoneNumberResponse(UserBot owner, UserBot requester, String phoneNumber, TelegramBot bot);
}
