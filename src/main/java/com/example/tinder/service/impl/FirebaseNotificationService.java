package com.example.tinder.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

    public String sendNotification(String fcmToken, String title, String body, String imageUrl, HashMap<String, String> data){
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(imageUrl)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .setToken(fcmToken)
                .putAllData(data)
                .build();

        try{
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Sending message to Firebase : {}", response);
            return response;
        }catch (Exception e){
            log.error("Error with push notification : {}", e.getMessage());
            return null;
        }
    }

}
