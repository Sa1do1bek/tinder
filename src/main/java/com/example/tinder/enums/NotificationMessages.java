package com.example.tinder.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationMessages {
    MESSAGE_1("Sizni sog‘indik 😊", "Yangi foydalanuvchilar sizni kutyapti"),
    MESSAGE_2("Yangi matchlar 👀", "Profilingizni tekshiring"),
    MESSAGE_3("Qaytish vaqti 🔥", "Kimlar sizni like qilganini ko‘ring"),
    MESSAGE_4("Bugun match topishingiz mumkin ❤️", "Profilingiz faol emas, kirib ko‘ring"),
    MESSAGE_5("Profilingizni sog‘inishyapti 😉", "Yangi suhbatlar sizni kutyapti"),
    MESSAGE_6("Kirish vaqti keldi ⚡", "Yangi suhbatlar sizni kutyapti");

    private final String title;
    private final String body;
}
