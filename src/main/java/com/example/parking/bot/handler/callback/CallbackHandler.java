package com.example.parking.bot.handler.callback;

import com.example.parking.entity.*;
import com.example.parking.service.*;
import com.example.parking.bot.keyboard.*;
import com.example.tinder.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final UserService userService;
    private final CarService carService;
    private final ContactService contactService;
    private final NotificationServices notificationService;
    private final KeyboardFactory keyboardFactory;
    private final TempCarDataService tempCarDataService;

    private final Map<Long, List<Integer>> callbackMessageIds = new ConcurrentHashMap<>();

    private void saveCallbackMessageId(Long chatId, Integer messageId) {
        callbackMessageIds.computeIfAbsent(chatId, k -> new ArrayList<>()).add(messageId);
    }

    private void deleteCallbackMessages(TelegramBot bot, Long chatId) {
        List<Integer> messageIds = callbackMessageIds.get(chatId);
        if (messageIds != null && !messageIds.isEmpty()) {
            for (int i = messageIds.size() - 1; i >= 0; i--) {
                try {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(chatId.toString());
                    deleteMessage.setMessageId(messageIds.get(i));
                    bot.execute(deleteMessage);

                    Thread.sleep(50);
                } catch (TelegramApiException e) {
                    System.out.println("Callback xabar o'chirishda xatolik (ID: " + messageIds.get(i) + "): " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            messageIds.clear();
        }
    }

    @Transactional
    public void handleCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        // Callback message ID'sini saqlaymiz
        saveCallbackMessageId(chatId, messageId);

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
        } else if (data.startsWith("car_manage_")) {
            handleCarManagement(user, data, chatId, messageId, bot);
        } else if (data.startsWith("car_edit_")) {
            handleCarEdit(user, data, chatId, messageId, bot);
        } else if (data.startsWith("car_delete_")) {
            handleCarDelete(user, data, chatId, messageId, bot);
        } else if (data.equals("cars_menu_back")) {
            handleCarsMenuBack(user, chatId, messageId, bot);
        } else if (data.startsWith("car_number_confirm_")) {
            handleCarNumberConfirmation(user, data, chatId, messageId, bot);
        }
    }

    private void handleCarNumberConfirmation(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        try {
            if (data.equals("car_number_confirm_no")) {
                if (user.getState() == UserState.WAITING_FIRST_CAR_CONFIRMATION) {
                    editMessage(bot, chatId, messageId,
                            "🚗 Avtomobilingizning davlat raqamini qayta kiriting:\n\n" +
                                    "📝 Format: 01 A 123 AA\n" +
                                    "💡 Masalan: 01 A 123 AA yoki 01A123AA");
                    userService.updateUserState(chatId, UserState.WAITING_FIRST_CAR_NUMBER);
                } else if (user.getState() == UserState.WAITING_CAR_CONFIRMATION) {
                    editMessage(bot, chatId, messageId,
                            "🚗 Yangi avtomobilingizning davlat raqamini qayta kiriting:\n\n" +
                                    "📝 Format: 01 A 123 AA\n" +
                                    "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                                    "🔙 Bekor qilish uchun 'Asosiy Menyu' tugmasini bosing.");
                    userService.updateUserState(chatId, UserState.WAITING_CAR_NUMBER);
                }
                tempCarDataService.delete(chatId);
                return;
            }

            if (data.startsWith("car_number_confirm_yes_")) {
                String licensePlate = data.substring("car_number_confirm_yes_".length()).replace("_", " ");

                Optional<TempCarData> tempDataOpt = tempCarDataService.findByChatId(chatId);
                if (tempDataOpt.isEmpty()) {
                    editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.");
                    return;
                }

                Car savedCar = carService.addCar(user, licensePlate, "Noma'lum", "Noma'lum", "Noma'lum");

                if (user.getState() == UserState.WAITING_FIRST_CAR_CONFIRMATION) {
                    String successMessage = String.format(
                            "✅ Avtomobilingiz muvaffaqiyatli ro'yxatga olindi!\n\n" +
                                    "🚗 Raqam: %s\n\n" +
                                    "🎉 Tabriklaymiz! Endi siz to'liq ro'yxatdan o'tdingiz.\n" +
                                    "Boshqa ma'lumotlarni 'Mening Avtomobillarim' bo'limida to'ldirishingiz mumkin.",
                            savedCar.getLicensePlate());

                    editMessage(bot, chatId, messageId, successMessage);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    showMainMenu(bot, chatId, "🏠 Asosiy menyu:");
                    userService.updateUserState(chatId, UserState.REGISTERED);

                } else if (user.getState() == UserState.WAITING_CAR_CONFIRMATION) {
                    String successMessage = String.format(
                            "✅ Avtomobil muvaffaqiyatli qo'shildi!\n\n" +
                                    "🚗 Raqam: %s\n\n" +
                                    "Endi boshqa foydalanuvchilar sizni topishi mumkin!",
                            savedCar.getLicensePlate());

                    editMessage(bot, chatId, messageId, successMessage);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    showCarsMenu(bot, chatId, "🚙 Avtomobillar menyusi:");
                    userService.updateUserState(chatId, UserState.CARS_MENU);
                }

                tempCarDataService.delete(chatId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi. Iltimos, qayta urinib ko'ring.");
        }
    }

    private void showMainMenu(TelegramBot bot, Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardFactory.getMainMenuKeyboard());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showCarsMenu(TelegramBot bot, Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardFactory.getCarsMenuKeyboard());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleReasonSelection(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
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

    private void handleContactRequest(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 3) return;

        String action = parts[1];
        Long requestId = Long.parseLong(parts[2]);

        switch (action) {
            case "phone":
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

    private void handleOwnerResponse(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 4) return;

        String action = parts[1] + "_" + parts[2];
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

    private void handlePhoneShare(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 4) return;

        String decision = parts[2];
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

    private void handlePrivacySettings(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        String[] parts = data.split("_");
        if (parts.length < 3) return;

        String setting = parts[1];

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

    private void handleCarManagement(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        try {
            Long carId = Long.parseLong(data.split("_")[2]);
            Optional<Car> carOpt = carService.findById(carId);

            if (carOpt.isEmpty() || !carOpt.get().getOwner().getChatId().equals(chatId)) {
                editMessage(bot, chatId, messageId, "❌ Avtomobil topilmadi yoki sizga tegishli emas.");
                return;
            }

            Car car = carOpt.get();
            String message = String.format(
                    "🚗 Avtomobil: %s\n" +
                            "🏷️ Marka: %s\n" +
                            "🚙 Model: %s\n" +
                            "🎨 Rang: %s\n\n" +
                            "Quyidagi amallardan birini tanlang:",
                    car.getLicensePlate(),
                    car.getBrand() != null ? car.getBrand() : "Noma'lum",
                    car.getModel() != null ? car.getModel() : "Noma'lum",
                    car.getColor() != null ? car.getColor() : "Noma'lum");

            editMessage(bot, chatId, messageId, message,
                    keyboardFactory.getCarManagementKeyboard(carId));

        } catch (Exception e) {
            e.printStackTrace();
            editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi.");
        }
    }

    private void handleCarEdit(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        try {
            Long carId = Long.parseLong(data.split("_")[2]);
            tempCarDataService.createOrUpdateForEdit(chatId, carId);

            editMessage(bot, chatId, messageId,
                    "✏️ Avtomobil markasini kiriting:\n\n" +
                            "💡 Masalan: Toyota, BMW, Chevrolet\n\n" +
                            "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");

            userService.updateUserState(chatId, UserState.WAITING_EDIT_CAR_BRAND);

        } catch (Exception e) {
            e.printStackTrace();
            editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi.");
        }
    }

    private void handleCarDelete(UserBot user, String data, Long chatId, Integer messageId, TelegramBot bot) {
        try {
            String[] parts = data.split("_");
            String action = parts[2];
            Long carId = Long.parseLong(parts[3]);

            if ("confirm".equals(action)) {
                Optional<Car> carOpt = carService.findById(carId);
                if (carOpt.isEmpty() || !carOpt.get().getOwner().getChatId().equals(chatId)) {
                    editMessage(bot, chatId, messageId, "❌ Avtomobil topilmadi.");
                    return;
                }

                Car car = carOpt.get();
                String message = String.format(
                        "⚠️ Diqqat!\n\n" +
                                "🚗 %s raqamli avtomobilni o'chirishni xohlaysizmi?\n\n" +
                                "Bu amal qaytarib bo'lmaydi!",
                        car.getLicensePlate());

                editMessage(bot, chatId, messageId, message,
                        keyboardFactory.getCarDeleteConfirmKeyboard(carId));

            } else if ("yes".equals(action)) {
                carService.deleteCar(carId, chatId);
                editMessage(bot, chatId, messageId, "✅ Avtomobil muvaffaqiyatli o'chirildi!");

                try {
                    Thread.sleep(2000);
                    showUserCarsCallback(user, chatId, messageId, bot);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            } else if ("no".equals(action)) {
                handleCarManagement(user, "car_manage_" + carId, chatId, messageId, bot);
            }

        } catch (Exception e) {
            e.printStackTrace();
            editMessage(bot, chatId, messageId, "❌ Xatolik yuz berdi.");
        }
    }

    private void handleCarsMenuBack(UserBot user, Long chatId, Integer messageId, TelegramBot bot) {
        showUserCarsCallback(user, chatId, messageId, bot);
    }

    private void showUserCarsCallback(UserBot user, Long chatId, Integer messageId, TelegramBot bot) {
        List<Car> cars = carService.findByOwner(chatId);

        if (cars.isEmpty()) {
            editMessage(bot, chatId, messageId,
                    "🚗 Sizda hali avtomobil ro'yxatdan o'tmagan.\n\n" +
                            "➕ Avtomobil qo'shish uchun asosiy menyuga o'ting.");
            return;
        }

        String message = "🚙 Sizning avtomobillaringiz:\n\n" +
                "Batafsil ma'lumot va boshqarish uchun avtomobilni tanlang:";

        editMessage(bot, chatId, messageId, message,
                keyboardFactory.getCarsListKeyboard(cars));
    }

    private void sendMessage(TelegramBot bot, Long chatId, String text,
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

    private void editMessage(TelegramBot bot, Long chatId, Integer messageId, String text) {
        editMessage(bot, chatId, messageId, text, null);
    }

    private void editMessage(TelegramBot bot, Long chatId, Integer messageId, String text,
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