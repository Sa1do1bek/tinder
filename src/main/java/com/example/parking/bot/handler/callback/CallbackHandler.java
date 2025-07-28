package com.example.parking.bot.handler.callback;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.keyboard.KeyboardFactory;
import com.example.parking.entity.Car;
import com.example.parking.entity.ContactReason;
import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.UserBot;
import com.example.parking.service.CarService;
import com.example.parking.service.ContactService;
import com.example.parking.service.NotificationServices;
import com.example.parking.service.UserService;
import com.example.tinder.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.Optional;

@Component
public class CallbackHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private ContactService contactService;

    @Autowired
    @Qualifier("parkingNotificationService")
    private NotificationServices notificationService;

    @Autowired
    private KeyboardFactory keyboardFactory;

    @Transactional
    public void handleCallbackQuery(CallbackQuery callbackQuery, ParkingTelegramBot bot) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        Optional<UserBot> userOpt = userService.findByChatId(chatId);
        if (userOpt.isEmpty()) {
            return;
        }

        UserBot user = userOpt.get();

        if (data.startsWith("reason_")) {
            handleReasonSelection(user, data, chatId, messageId, bot);
        } else if (data.startsWith("contact_")) {
            handleContactRequest(user, data, chatId, messageId, bot);
        } else if (data.startsWith("owner_")) {
            handleOwnerResponse(user, data, chatId, messageId, bot);
        } else if (data.startsWith("phone_share_")) {
            handlePhoneShare(user, data, chatId, messageId, bot);
        } else if (data.startsWith("privacy_")) {
            handlePrivacySettings(user, data, chatId, messageId, bot);
        }
    }

    private void handleReasonSelection(UserBot user, String data, Long chatId, Integer messageId, ParkingTelegramBot bot) {
        try {
            String[] parts = data.split("_", 3);
            if (parts.length < 3) {
                editMessage(bot, chatId, messageId, "❌ Noto'g'ri ma'lumot format.");
                return;
            }

            String reasonCode = parts[1] + "_" + parts[2].split("_")[0];
            String licensePlate = parts[2].substring(parts[2].indexOf("_") + 1).replace("_", " ");

            ContactReason reason;
            try {
                reason = ContactReason.valueOf(reasonCode);
            } catch (IllegalArgumentException e) {
                editMessage(bot, chatId, messageId, "❌ Noto'g'ri sabab kodi.");
                return;
            }

            Optional<Car> carOpt = carService.findByLicensePlate(licensePlate);
            if (carOpt.isEmpty()) {
                editMessage(bot, chatId, messageId, "❌ Avtomobil topilmadi.");
                return;
            }

            Car car = carOpt.get();
            UserBot owner = car.getOwner();

            if (!contactService.canSendRequest(user.getChatId(), licensePlate)) {
                editMessage(bot, chatId, messageId, "⏰ Siz juda ko'p so'rov yubordingiz. Iltimos, keyinroq urinib ko'ring.");
                return;
            }

            ContactRequest request = contactService.createContactRequest(user, owner, licensePlate, reason, null);

            // Send notification to car owner
            notificationService.sendContactRequestNotification(request, bot);

            String responseText = "✅ Murojaat yuborildi!\n\n" +
                    "📨 Avtomobil egasiga xabar yuborildi.\n" +
                    "⏰ Tez orada javob olasiz.\n\n" +
                    "Aloqa o'rnatish uchun quyidagi variantlardan foydalaning:";

            editMessage(bot, chatId, messageId, responseText,
                    keyboardFactory.getContactOptionsKeyboard(owner, request.getId()));

        } catch (Exception e) {
            e.printStackTrace();
            editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi. Iltimos, qayta urinib ko'ring.");
        }
    }

    private void handleContactRequest(UserBot user, String data, Long chatId, Integer messageId, ParkingTelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 3) return;

        String action = parts[1];
        Long requestId = Long.parseLong(parts[2]);

        switch (action) {
            case "phone":
                // Request phone number from owner
                Optional<ContactRequest> requestOpt = contactService.findById(requestId);
                if (requestOpt.isPresent()) {
                    ContactRequest request = requestOpt.get();
                    notificationService.sendPhoneNumberRequest(user, request.getReceiver(), request.getLicensePlate(), bot);
                    editMessage(bot, chatId, messageId, "📞 Telefon raqami so'rovi yuborildi. Kutib turing...");
                }
                break;
            case "resolve":
                contactService.resolveRequest(requestId);
                editMessage(bot, chatId, messageId, "✅ Muammo hal bo'ldi deb belgilandi. Rahmat!");
                break;
        }
    }

    private void handleOwnerResponse(UserBot user, String data, Long chatId, Integer messageId, ParkingTelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 4) return;

        String action = parts[1] + "_" + parts[2]; // allow_phone or resolve
        Long requestId = Long.parseLong(parts[3]);

        switch (action) {
            case "allow_phone":
                Optional<ContactRequest> requestOpt = contactService.findById(requestId);
                if (requestOpt.isPresent()) {
                    ContactRequest request = requestOpt.get();
                    notificationService.sendPhoneNumberResponse(user, request.getSender(), user.getPhoneNumber(), bot);
                    editMessage(bot, chatId, messageId, "✅ Telefon raqamingiz ulashildi!");
                }
                break;
            case "resolve":
                contactService.resolveRequest(requestId);
                editMessage(bot, chatId, messageId, "✅ Muammo hal qilindi!");
                break;
        }
    }

    private void handlePhoneShare(UserBot user, String data, Long chatId, Integer messageId, ParkingTelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 4) return;

        String decision = parts[2]; // yes or no
        Long requesterChatId = Long.parseLong(parts[3]);

        if ("yes".equals(decision)) {
            Optional<UserBot> requesterOpt = userService.findByChatId(requesterChatId);
            if (requesterOpt.isPresent()) {
                notificationService.sendPhoneNumberResponse(user, requesterOpt.get(), user.getPhoneNumber(), bot);
                editMessage(bot, chatId, messageId, "✅ Telefon raqamingiz ulashildi!");
            }
        } else {
            editMessage(bot, chatId, messageId, "❌ Telefon raqami ulashilmadi.");
        }
    }

    private void handlePrivacySettings(UserBot user, String data, Long chatId, Integer messageId, ParkingTelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 3) return;

        String setting = parts[1]; // username or phone

        if ("username".equals(setting)) {
            boolean newValue = !user.getShareUsername();
            userService.updatePrivacySettings(chatId, newValue, user.getSharePhoneNumber());
            user.setShareUsername(newValue);
        } else if ("phone".equals(setting)) {
            boolean newValue = !user.getSharePhoneNumber();
            userService.updatePrivacySettings(chatId, user.getShareUsername(), newValue);
            user.setSharePhoneNumber(newValue);
        }

        String message = String.format("🔒 Maxfiylik sozlamalari:\n\n" +
                        "👤 Username ulashish: %s\n" +
                        "📱 Telefon raqami ulashish: %s\n" +
                        "🔕 Bezovta qilmaslik: %s",
                user.getShareUsername() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getSharePhoneNumber() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getDoNotDisturb() ? "✅ Yoqilgan" : "❌ O'chirilgan");

        editMessage(bot, chatId, messageId, message, keyboardFactory.getPrivacySettingsKeyboard(user));
    }

    private void sendMessage(ParkingTelegramBot bot, Long chatId, String text,
                             InlineKeyboardMarkup keyboard) {
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

    private void editMessage(ParkingTelegramBot bot, Long chatId, Integer messageId, String text) {
        editMessage(bot, chatId, messageId, text, null);
    }

    private void editMessage(ParkingTelegramBot bot, Long chatId, Integer messageId, String text,
                             InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setText(text);

        if (keyboard != null) {
            editMessage.setReplyMarkup(keyboard);
        }

        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
