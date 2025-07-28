package com.example.parking.service;


import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.UserBot;

public interface NotificationServices {
    void sendContactRequestNotification(ContactRequest request, ParkingTelegramBot bot);
    void sendPhoneNumberRequest(UserBot requester, UserBot owner, String licensePlate, ParkingTelegramBot bot);
    void sendPhoneNumberResponse(UserBot owner, UserBot requester, String phoneNumber, ParkingTelegramBot bot);
}
