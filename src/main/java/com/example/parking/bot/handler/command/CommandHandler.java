package com.example.parking.bot.handler.command;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.keyboard.KeyboardFactory;
import com.example.parking.entity.*;
import com.example.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Component
public class CommandHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private TempCarDataService tempCarDataService;

    @Autowired
    private KeyboardFactory keyboardFactory;

    @Autowired
    private OilChangeService oilChangeService;

    private final Map<Long, List<Integer>> contentMessageIds = new ConcurrentHashMap<>();

    private final Map<Long, Integer> mainMenuMessageIds = new ConcurrentHashMap<>();
    private final Map<Long, Integer> settingsMenuMessageIds = new ConcurrentHashMap<>();
    private final Map<Long, Integer> carsMenuMessageIds = new ConcurrentHashMap<>();
    private final Map<Long, Integer> phoneRequestMessageIds = new ConcurrentHashMap<>();

    private void saveContentMessageId(Long chatId, Integer messageId) {
        contentMessageIds.computeIfAbsent(chatId, k -> new ArrayList<>()).add(messageId);
    }

    private void deleteContentMessages(ParkingTelegramBot bot, Long chatId) {
        List<Integer> messageIds = contentMessageIds.get(chatId);
        if (messageIds != null && !messageIds.isEmpty()) {
            for (int i = messageIds.size() - 1; i >= 0; i--) {
                try {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(chatId.toString());
                    deleteMessage.setMessageId(messageIds.get(i));
                    bot.execute(deleteMessage);

                    Thread.sleep(50);
                } catch (TelegramApiException e) {
                    System.out.println("Content xabar o'chirishda xatolik (ID: " + messageIds.get(i) + "): " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            messageIds.clear();
        }
    }

    private void deleteOldMenuMessages(ParkingTelegramBot bot, Long chatId) {
        Integer oldMainMenu = mainMenuMessageIds.get(chatId);
        if (oldMainMenu != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldMainMenu);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski asosiy menyu o'chirishda xatolik: " + e.getMessage());
            }
        }

        Integer oldSettingsMenu = settingsMenuMessageIds.get(chatId);
        if (oldSettingsMenu != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldSettingsMenu);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski sozlamalar menyu o'chirishda xatolik: " + e.getMessage());
            }
        }

        Integer oldCarsMenu = carsMenuMessageIds.get(chatId);
        if (oldCarsMenu != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldCarsMenu);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski avtomobillar menyu o'chirishda xatolik: " + e.getMessage());
            }
        }

        Integer oldPhoneRequest = phoneRequestMessageIds.get(chatId);
        if (oldPhoneRequest != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldPhoneRequest);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski telefon so'rovi o'chirishda xatolik: " + e.getMessage());
            }
        }
    }

    private void sendContentMessage(ParkingTelegramBot bot, Long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        deleteContentMessages(bot, chatId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        if (inlineKeyboard != null) {
            message.setReplyMarkup(inlineKeyboard);
        }

        try {
            Message sentMessage = bot.execute(message);
            saveContentMessageId(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu(ParkingTelegramBot bot, Long chatId, String text) {
        deleteContentMessages(bot, chatId);

        deleteOldMenuMessages(bot, chatId);

        String enhancedText = text + "\n\n💡 Avtomobil egasini topish uchun avtomobil raqamini yuboring (masalan: 01 A 123 AA)";

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(enhancedText);
        message.setReplyMarkup(keyboardFactory.getMainMenuKeyboard());

        try {
            Message sentMessage = bot.execute(message);
            mainMenuMessageIds.put(chatId, sentMessage.getMessageId());
            settingsMenuMessageIds.remove(chatId);
            carsMenuMessageIds.remove(chatId);
            phoneRequestMessageIds.remove(chatId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showSettingsMenu(ParkingTelegramBot bot, Long chatId, String text) {
        deleteContentMessages(bot, chatId);

        Integer oldSettingsMenu = settingsMenuMessageIds.get(chatId);
        if (oldSettingsMenu != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldSettingsMenu);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski sozlamalar menyu o'chirishda xatolik: " + e.getMessage());
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardFactory.getSettingsKeyboard());

        try {
            Message sentMessage = bot.execute(message);
            settingsMenuMessageIds.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showCarsMenu(ParkingTelegramBot bot, Long chatId, String text) {
        deleteContentMessages(bot, chatId);

        Integer oldCarsMenu = carsMenuMessageIds.get(chatId);
        if (oldCarsMenu != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(oldCarsMenu);
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Eski avtomobillar menyu o'chirishda xatolik: " + e.getMessage());
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardFactory.getCarsMenuKeyboard());

        try {
            Message sentMessage = bot.execute(message);
            carsMenuMessageIds.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showPhoneRequest(ParkingTelegramBot bot, Long chatId, String text) {
        deleteContentMessages(bot, chatId);

        deleteOldMenuMessages(bot, chatId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardFactory.getPhoneRequestKeyboard());

        try {
            Message sentMessage = bot.execute(message);
            phoneRequestMessageIds.put(chatId, sentMessage.getMessageId());
            mainMenuMessageIds.remove(chatId);
            settingsMenuMessageIds.remove(chatId);
            carsMenuMessageIds.remove(chatId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void handleTextMessage(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();
        String text = message.getText();

        saveContentMessageId(chatId, message.getMessageId());

        Optional<UserBot> userOpt = userService.findByChatId(chatId);

        if (text.equals("/start")) {
            handleStartCommand(message, bot);
            return;
        }

        if (userOpt.isEmpty()) {
            sendContentMessage(bot, chatId, "Iltimos, avval /start buyrug'ini bosing.", null);
            return;
        }

        UserBot user = userOpt.get();

        if (handleUniversalButtons(user, text, bot)) {
            return;
        }

        switch (user.getState()) {
            case NEW:
                handleNewUserState(user, text, bot);
                break;
            case WAITING_PHONE:
                handleWaitingPhoneState(user, text, bot);
                break;
            case WAITING_FIRST_CAR_NUMBER:
                handleFirstCarNumberInput(user, text, bot);
                break;
            case REGISTERED:
                handleRegisteredUserCommands(user, text, bot);
                break;
            case CARS_MENU:
                handleCarsMenuCommands(user, text, bot);
                break;
            case WAITING_LICENSE_PLATE:
                handleLicensePlateInput(user, text, bot);
                break;
            case WAITING_CAR_NUMBER:
                handleCarNumberInput(user, text, bot);
                break;
            case WAITING_CAR_BRAND:
                handleCarBrandInput(user, text, bot);
                break;
            case WAITING_CAR_MODEL:
                handleCarModelInput(user, text, bot);
                break;
            case WAITING_CAR_COLOR:
                handleCarColorInput(user, text, bot);
                break;
            case SETTINGS_MENU:
                handleSettingsCommands(user, text, bot);
                break;
            case OIL_WAITING_LAST_DATE:
                handleOilWaitingLastDate(user, text, bot);
                break;
            case OIL_WAITING_LAST_MILEAGE:
                handleOilWaitingLastMileage(user, text, bot);
                break;
            case OIL_WAITING_BRAND:
                handleOilWaitingBrand(user, text, bot);
                break;
            case OIL_WAITING_RECOMMENDED_KM:
                handleOilWaitingRecommendedKm(user, text, bot);
                break;
            case OIL_WAITING_DAILY_DISTANCE:
                handleOilWaitingDailyDistance(user, text, bot);
                break;
            case WAITING_EDIT_CAR_BRAND:
                handleWaitingEditCarBrand(user, text, bot);
                break;
            case WAITING_EDIT_CAR_MODEL:
                handleWaitingEditCarModel(user, text, bot);
                break;
            case WAITING_EDIT_CAR_COLOR:
                handleWaitingEditCarColor(user, text, bot);
                break;
            default:
                handleRegisteredUserCommands(user, text, bot);
        }
    }

    private boolean handleUniversalButtons(UserBot user, String text, ParkingTelegramBot bot) {
        if (user.getState() == UserState.REGISTERED && carService.isValidLicensePlate(text)) {
            handleLicensePlateInput(user, text, bot);
            return true;
        }
        switch (text) {
            case "🏠 Asosiy Menyu":
            case "🔙 Asosiy Menyu":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                return true;

            case "🚙 Mening Avtomobillarim":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showCarsMenu(bot, user.getChatId(), "🚙 Avtomobillar menyusi:");
                userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
                return true;

            case "🚗 Avtomobil Egasini Topish":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                sendContentMessage(bot, user.getChatId(), "🔍 Qidirilayotgan avtomobilning davlat raqamini kiriting:\n\n" +
                        "📝 Format: 01 A 123 AA\n" +
                        "💡 Masalan: 01 A 123 AA yoki 01A123AA", null);
                userService.updateUserState(user.getChatId(), UserState.WAITING_LICENSE_PLATE);
                return true;

            case "📊 Tarix":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showRequestHistory(user, bot);
                return true;

            case "⚙️ Sozlamalar":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showSettingsMenu(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:");
                userService.updateUserState(user.getChatId(), UserState.SETTINGS_MENU);
                return true;

            case "🛡 Sug'urta":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showSugurta(user, bot);
                return true;

            case "🔧 Texnik ko'rik":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showTexnikKorik(user, bot);
                return true;

            case "🛢 Avtomobil moyi":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                startOilChangeProcess(user, bot);
                return true;

            case "📋 Avtomobillarimni Ko'rish":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showUserCars(user, bot);
                return true;

            case "➕ Avtomobil Qo'shish":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                tempCarDataService.createOrUpdate(user.getChatId());
                sendContentMessage(bot, user.getChatId(),
                        "🚗 Yangi avtomobilingizning davlat raqamini kiriting:\n\n" +
                                "📝 Format: 01 A 123 AA\n" +
                                "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                                "🔙 Bekor qilish uchun 'Asosiy Menyu' tugmasini bosing.", null);
                userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_NUMBER);
                return true;

            case "🔒 Maxfiylik Sozlamalari":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                showPrivacySettings(user, bot);
                return true;

            case "🔕 Bezovta Qilmaslik":
                tempCarDataService.delete(user.getChatId());
                oilChangeService.deleteTempData(user.getChatId());

                toggleDoNotDisturb(user, bot);
                return true;

            default:
                return false;
        }
    }

    private void startFirstCarRegistration(UserBot user, ParkingTelegramBot bot) {
        tempCarDataService.createOrUpdate(user.getChatId());
        sendContentMessage(bot, user.getChatId(),
                "🚗 Avtomobilingizning davlat raqamini kiriting:\n\n" +
                        "📝 Format: 01 A 123 AA\n" +
                        "💡 Masalan: 01 A 123 AA yoki 01A123AA", null);
        userService.updateUserState(user.getChatId(), UserState.WAITING_FIRST_CAR_NUMBER);
    }


    private void handleFirstCarNumberInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (!carService.isValidLicensePlate(text)) {
            sendContentMessage(bot, user.getChatId(),
                    "❌ Noto'g'ri format! Iltimos, avtomobil raqamini to'g'ri formatda kiriting:\n\n" +
                            "📝 Format: 01 A 123 AA\n" +
                            "💡 Masalan: 01 A 123 AA yoki 01A123AA", null);
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);

        if (carService.isLicensePlateExists(formattedPlate)) {
            sendContentMessage(bot, user.getChatId(),
                    "❌ Bu raqamli avtomobil allaqachon ro'yxatdan o'tgan!\n\n" +
                            "Iltimos, boshqa raqam kiriting.", null);
            return;
        }

        tempCarDataService.updateLicensePlate(user.getChatId(), formattedPlate);

        String confirmMessage = String.format(
                "✅ Kiritilgan avtomobil raqami:\n\n" +
                        "🚗 %s\n\n" +
                        "Bu raqam to'g'rimi?",
                formattedPlate);

        sendContentMessage(bot, user.getChatId(), confirmMessage,
                keyboardFactory.getCarNumberConfirmationKeyboard(formattedPlate));

        userService.updateUserState(user.getChatId(), UserState.WAITING_FIRST_CAR_CONFIRMATION);
    }

    private void handleCarNumberInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish")) {
            showCarsMenu(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🚙 Avtomobillar menyusi:");
            userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (!carService.isValidLicensePlate(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, avtomobil raqamini to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                    "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);

        if (carService.isLicensePlateExists(formattedPlate)) {
            sendContentMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil allaqachon ro'yxatdan o'tgan!\n\n" +
                    "🔙 Orqaga qaytish uchun 'Orqaga' tugmasini bosing.", null);
            return;
        }

        tempCarDataService.updateLicensePlate(user.getChatId(), formattedPlate);

        String confirmMessage = String.format(
                "✅ Kiritilgan avtomobil raqami:\n\n" +
                        "🚗 %s\n\n" +
                        "Bu raqam to'g'rimi?",
                formattedPlate);

        sendContentMessage(bot, user.getChatId(), confirmMessage,
                keyboardFactory.getCarNumberConfirmationKeyboard(formattedPlate));

        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_CONFIRMATION);
    }
    private void handleCarsMenuCommands(UserBot user, String text, ParkingTelegramBot bot) {
        switch (text) {
            case "📋 Avtomobillarimni Ko'rish":
                showUserCars(user, bot);
                break;
            case "➕ Avtomobil Qo'shish":
                tempCarDataService.createOrUpdate(user.getChatId());
                sendContentMessage(bot, user.getChatId(),
                        "🚗 Yangi avtomobilingizning davlat raqamini kiriting:\n\n" +
                                "📝 Format: 01 A 123 AA\n" +
                                "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                                "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
                userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_NUMBER);
                break;
            case "🔙 Asosiy Menyu":
                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
            default:
                showCarsMenu(bot, user.getChatId(), "🚙 Avtomobillar menyusi:");
        }
    }

    private void startOilChangeProcess(UserBot user, ParkingTelegramBot bot) {
        try {
            oilChangeService.createOrUpdateTempData(user.getChatId());

            String message = """
            🛢 Avtomobil moyi ma'lumotlari
            
            Iltimos, oxirgi marta moy almashtirilgan sanani kiriting:
            
            📅 Format: dd.MM.yyyy
            💡 Masalan: 15.06.2025
            
            🔙 Bekor qilish uchun /start buyrug'ini yuboring.
            """;

            sendContentMessage(bot, user.getChatId(), message, null);
            userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_LAST_DATE);
        } catch (Exception e) {
            e.printStackTrace();
            showMainMenu(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:");
        }
    }

    private void handleOilWaitingLastDate(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidDate(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri sana formati! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "📅 Format: dd.MM.yyyy\n" +
                    "💡 Masalan: 15.06.2025\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        LocalDate lastDate = oilChangeService.parseDate(text);
        oilChangeService.updateTempLastDate(user.getChatId(), lastDate);

        String message = """
        ✅ Sana saqlandi!
        
        Endi o'sha vaqtdagi avtomobil probegin kiriting:
        
        🚗 Probeg (km da)
        💡 Masalan: 125000
        
        🔙 Bekor qilish uchun /start buyrug'ini yuboring.
        """;

        sendContentMessage(bot, user.getChatId(), message, null);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_LAST_MILEAGE);
    }

    private void handleOilWaitingLastMileage(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidMileage(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri probeg formati! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "🚗 Faqat raqam kiriting\n" +
                    "💡 Masalan: 125000\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        int mileage = oilChangeService.parseMileage(text);
        oilChangeService.updateTempLastMileage(user.getChatId(), mileage);

        String message = """
        ✅ Probeg saqlandi!
        
        Moy markasini kiriting:
        
        🛢 Moy markasi
        💡 Masalan: ZIC X9, Castrol GTX, Shell Helix
        
        🔙 Bekor qilish uchun /start buyrug'ini yuboring.
        """;

        sendContentMessage(bot, user.getChatId(), message, null);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_BRAND);
    }

    private void handleOilWaitingBrand(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendContentMessage(bot, user.getChatId(), "❌ Iltimos, moy markasini to'liq kiriting.\n\n" +
                    "💡 Masalan: ZIC X9, Castrol GTX, Shell Helix\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        oilChangeService.updateTempOilBrand(user.getChatId(), text.trim());

        String message = """
        ✅ Moy markasi saqlandi!
        
        Necha kilometrda moy almashtirish tavsiya etiladi?
        
        📏 Tavsiya etilgan interval (km)
        💡 Masalan: 8000, 10000, 15000
        
        🔙 Bekor qilish uchun /start buyrug'ini yuboring.
        """;

        sendContentMessage(bot, user.getChatId(), message, null);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_RECOMMENDED_KM);
    }

    private void handleOilWaitingRecommendedKm(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidRecommendedKm(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri interval! Iltimos, to'g'ri formatda kiriting:\n" +
                    "📏 1000 dan 50000 gacha\n" +
                    "💡 Masalan: 8000, 10000, 15000\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        int recommendedKm = oilChangeService.parseMileage(text);
        oilChangeService.updateTempRecommendedKm(user.getChatId(), recommendedKm);

        String message = """
        ✅ Interval saqlandi!
        
        Har kuni o'rtacha necha kilometr yurasiz?
        
        🚗 Kunlik o'rtacha masofa (km)
        💡 Masalan: 40, 50, 100
        
        🔙 Bekor qilish uchun /start buyrug'ini yuboring.
        """;

        sendContentMessage(bot, user.getChatId(), message, null);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_DAILY_DISTANCE);
    }

    private void handleOilWaitingDailyDistance(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidDailyDistance(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri kunlik masofa! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "🚗 1 dan 1000 gacha\n" +
                    "💡 Masalan: 40, 50, 100\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        int dailyDistance = oilChangeService.parseMileage(text);
        oilChangeService.updateTempDailyDistance(user.getChatId(), dailyDistance);

        Optional<TempOilChangeData> tempDataOpt = oilChangeService.getTempData(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            showMainMenu(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        TempOilChangeData tempData = tempDataOpt.get();
        OilChangeInfo savedInfo = oilChangeService.saveOilChangeInfo(user, tempData);

        String resultMessage = String.format("""
        ✅ Ma'lumotlar saqlandi!
        
        📊 Kiritilgan ma'lumotlar:
        📅 Oxirgi almashtirish: %s
        🚗 O'sha vaqtdagi probeg: %s
        🛢 Moy markasi: %s
        📏 Tavsiya etilgan interval: %s
        🚗 Kunlik o'rtacha: %d km
        
        🔮 Keyingi moy almashtirish:
        📅 Sana: %s
        🚗 Probeg: %s
        
        💡 Eslatma: Bu taxminiy hisob-kitob. Haydash sharoitlariga qarab o'zgarishi mumkin.
        """,
                oilChangeService.formatDate(savedInfo.getLastChangeDate()),
                oilChangeService.formatMileage(savedInfo.getLastChangeMileage()),
                savedInfo.getOilBrand(),
                oilChangeService.formatMileage(savedInfo.getRecommendedKm()),
                savedInfo.getDailyDistance(),
                oilChangeService.formatDate(savedInfo.getNextChangeDate()),
                oilChangeService.formatMileage(savedInfo.getNextChangeMileage())
        );

        sendContentMessage(bot, user.getChatId(), resultMessage, null);
        userService.updateUserState(user.getChatId(), UserState.REGISTERED);
        oilChangeService.deleteTempData(user.getChatId());
    }

    private void showTexnikKorik(UserBot user, ParkingTelegramBot bot) {
        try {
            String message = """
                🔧 Texnik ko'rik haqida ma'lumot:
                
                🔹 Texnik ko'rik muddatlari:
                   • Yangi avtomobillar: 3 yil
                   • 3-7 yil: har 2 yilda
                   • 7 yildan yuqori: har yili
                
                🔹 Kerakli hujjatlar:
                   • Texnik passport
                   • Haydovchilik guvohnomasi
                   • Sug'urta polisi
                   • Oldingi texnik ko'rik guvohnomasi
                
                🔹 Tekshiriladigan qismlar:
                   • Tormoz tizimi
                   • Yo'l yoritgichlari
                   • Rulda boshqaruv
                   • Dvigatel holati
                   • Ekologik ko'rsatkichlar
                
                🏢 Rasmiy texnik ko'rik punktlariga murojaat qiling.
                """;

            sendContentMessage(bot, user.getChatId(), message, null);
        } catch (Exception e) {
            e.printStackTrace();
            sendContentMessage(bot, user.getChatId(), "❌ Ma'lumot yuklashda xatolik yuz berdi.", null);
        }
    }

    private void showSugurta(UserBot user, ParkingTelegramBot bot) {
        try {
            String text = """
                🛡 Sug'urta haqida ma'lumot:
                
                Siz quyidagi sug'urta kompaniyalari rasmiy saytlaridan kerakli ma'lumotlarni olishingiz mumkin:
                
                🔹 Majburiy texnik ko'rik
                🔹 Sug'urta narxlari va shartlari
                🔹 Onlayn sug'urta rasmiylashtirish
                🔹 Sug'urta to'lovlari va kompensatsiyalar
                
                Quyidagi havolalar orqali rasmiy saytlarga o'ting:
                """;

            InlineKeyboardMarkup keyboard = keyboardFactory.getInsuranceLinksKeyboard();
            sendContentMessage(bot, user.getChatId(), text, keyboard);
        } catch (Exception e) {
            e.printStackTrace();
            sendContentMessage(bot, user.getChatId(), "❌ Ma'lumot yuklashda xatolik yuz berdi.", null);
        }
    }

    public void handleContactMessage(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();

        saveContentMessageId(chatId, message.getMessageId());

        Optional<UserBot> userOpt = userService.findByChatId(chatId);

        if (userOpt.isEmpty()) {
            sendContentMessage(bot, chatId, "Iltimos, avval /start buyrug'ini bosing.", null);
            return;
        }

        UserBot user = userOpt.get();
        if (user.getState() == UserState.WAITING_PHONE) {
            String phoneNumber = message.getContact().getPhoneNumber();
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }

            if (userService.isPhoneNumberExists(phoneNumber)) {
                sendContentMessage(bot, chatId, "Bu telefon raqami allaqachon ro'yxatdan o'tgan. Boshqa raqam kiriting.", null);
                return;
            }

            userService.updatePhoneNumber(chatId, phoneNumber);

            startFirstCarRegistration(user, bot);
        }
    }

    private void handleStartCommand(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();

        saveContentMessageId(chatId, message.getMessageId());

        Optional<UserBot> userOpt = userService.findByChatId(chatId);

        if (userOpt.isEmpty()) {
            UserBot user = userService.createUser(
                    chatId,
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName(),
                    message.getFrom().getUserName()
            );

            String welcomeText = "🚗 Assalomu alaykum! Parking Bot'ga xush kelibsiz!\n\n" +
                    "Bu bot avtomobil egalarini topish va ular bilan aloqa o'rnatishga yordam beradi.\n\n" +
                    "🔹 Noto'g'ri parklangan avtomobil egasini toping\n" +
                    "🔹 O'z avtomobillaringizni ro'yxatga oling\n" +
                    "🔹 Xavfsiz va tez aloqa o'rnating\n\n" +
                    "Ro'yxatdan o'tish uchun telefon raqamingizni ulashing:";

            showPhoneRequest(bot, chatId, welcomeText);
            userService.updateUserState(chatId, UserState.WAITING_PHONE);
        } else {
            UserBot user = userOpt.get();

            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() &&
                    user.getState() == UserState.REGISTERED) {
                showMainMenu(bot, chatId, "🏠 Xush kelibsiz! Quyidagi menyudan kerakli bo'limni tanlang:");
            } else {
                if (user.getState() == UserState.WAITING_PHONE || user.getPhoneNumber() == null) {
                    String welcomeText = "📱 Ro'yxatdan o'tishni yakunlash uchun telefon raqamingizni ulashing:";
                    showPhoneRequest(bot, chatId, welcomeText);
                    userService.updateUserState(chatId, UserState.WAITING_PHONE);
                } else {
                    handleUserByState(user, bot);
                }
            }
        }
    }

    private void handleUserByState(UserBot user, ParkingTelegramBot bot) {
        switch (user.getState()) {
            case REGISTERED:
                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                break;
            case WAITING_PHONE:
                showPhoneRequest(bot, user.getChatId(), "📱 Telefon raqamingizni ulash tugmasini bosing.");
                break;
            case SETTINGS_MENU:
                showSettingsMenu(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:");
                break;
            case CARS_MENU:
                showCarsMenu(bot, user.getChatId(), "🚙 Avtomobillar menyusi:");
                break;
            default:
                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
        }
    }

    private void handleNewUserState(UserBot user, String text, ParkingTelegramBot bot) {
        String welcomeText = "📱 Ro'yxatdan o'tish uchun telefon raqamingizni ulashing:";
        showPhoneRequest(bot, user.getChatId(), welcomeText);
        userService.updateUserState(user.getChatId(), UserState.WAITING_PHONE);
    }

    private void handleWaitingPhoneState(UserBot user, String text, ParkingTelegramBot bot) {
        sendContentMessage(bot, user.getChatId(), "📱 Iltimos, telefon raqamingizni ulash tugmasini bosing.", null);
    }

    private void handleRegisteredUserCommands(UserBot user, String text, ParkingTelegramBot bot) {
        if (carService.isValidLicensePlate(text)) {
            handleLicensePlateInput(user, text, bot);
            return;
        }
        switch (text) {

            case "🚙 Mening Avtomobillarim":
                showCarsMenu(bot, user.getChatId(), "🚙 Avtomobillar menyusi:");
                userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
                break;

            case "📊 Tarix":
                showRequestHistory(user, bot);
                break;

            case "⚙️ Sozlamalar":
                showSettingsMenu(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:");
                userService.updateUserState(user.getChatId(), UserState.SETTINGS_MENU);
                break;

            case "🔙 Asosiy Menyu":
                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;

            case "🛡 Sug'urta":
                showSugurta(user, bot);
                break;

            case "🔧 Texnik ko'rik":
                showTexnikKorik(user, bot);
                break;

            case "🛢 Avtomobil moyi":
                startOilChangeProcess(user, bot);
                break;

            default:
                showMainMenu(bot, user.getChatId(), "❓ Noma'lum buyruq. Iltimos, menyudan tanlang.\n\n🏠 Asosiy menyu:");
        }
    }

    private void handleSettingsCommands(UserBot user, String text, ParkingTelegramBot bot) {
        switch (text) {
            case "🔒 Maxfiylik Sozlamalari":
                showPrivacySettings(user, bot);
                break;
            case "🔕 Bezovta Qilmaslik":
                toggleDoNotDisturb(user, bot);
                break;
            case "🔙 Asosiy Menyu":
                showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
            default:
                showSettingsMenu(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:");
        }
    }

    private void handleLicensePlateInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu")) {
            showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        if (text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        if (!carService.isValidLicensePlate(text)) {
            sendContentMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, avtomobil raqamini to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123"+
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);
        Optional<Car> carOpt = carService.findByLicensePlate(formattedPlate);

        if (carOpt.isEmpty()) {
            sendContentMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil topilmadi.\n\n" +
                    "🔍 Avtomobil egasi hali botdan ro'yxatdan o'tmagan bo'lishi mumkin.\n" +
                    "💡 Ularni botdan foydalanishga taklif qiling!\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.", null);
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        Car car = carOpt.get();
        UserBot owner = car.getOwner();

        if (owner.getChatId().equals(user.getChatId())) {
            sendContentMessage(bot, user.getChatId(), "ℹ️ Bu sizning avtomobilingiz!\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.", null);
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        String message = String.format("✅ Avtomobil topildi!\n\n🚗 Raqam: %s\n🏷️ Marka: %s\n🎨 Rang: %s\n\n📝 Murojaat sababini tanlang:",
                formattedPlate,
                car.getBrand() != null ? car.getBrand() : "Ko'rsatilmagan",
                car.getColor() != null ? car.getColor() : "Ko'rsatilmagan");

        sendContentMessage(bot, user.getChatId(), message,
                keyboardFactory.getContactReasonKeyboard(formattedPlate));

        userService.updateUserState(user.getChatId(), UserState.REGISTERED);
    }


    private void handleCarBrandInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish")) {
            showCarsMenu(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🚙 Avtomobillar menyusi:");
            userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendContentMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil markasini to'liq kiriting.\n\n" +
                    "💡 Masalan: Toyota, BMW, Chevrolet\n\n" +
                    "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
            return;
        }

        tempCarDataService.updateBrand(user.getChatId(), text.trim());
        sendContentMessage(bot, user.getChatId(), "🚗 Avtomobil modelini kiriting:\n\n💡 Masalan: Camry, X5, Malibu\n\n" +
                "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_MODEL);
    }

    private void handleCarModelInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish")) {
            showCarsMenu(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🚙 Avtomobillar menyusi:");
            userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 1) {
            sendContentMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil modelini kiriting.\n\n" +
                    "💡 Masalan: Camry, X5, Malibu\n\n" +
                    "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
            return;
        }

        tempCarDataService.updateModel(user.getChatId(), text.trim());
        sendContentMessage(bot, user.getChatId(), "🎨 Avtomobil rangini kiriting:\n\n💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_COLOR);
    }

    private void handleCarColorInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish")) {
            showCarsMenu(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🚙 Avtomobillar menyusi:");
            userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendContentMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil rangini kiriting.\n\n" +
                    "💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                    "🔙 Bekor qilish uchun 'Orqaga' tugmasini bosing.", null);
            return;
        }

        Optional<TempCarData> tempDataOpt = tempCarDataService.findByChatId(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            showCarsMenu(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🚙 Avtomobillar menyusi:");
            userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
            return;
        }

        TempCarData tempData = tempDataOpt.get();
        tempCarDataService.updateColor(user.getChatId(), text.trim());

        Car savedCar = carService.addCar(user, tempData.getLicensePlate(), tempData.getBrand(), tempData.getModel(), text.trim());

        String successMessage = String.format("✅ Avtomobil muvaffaqiyatli qo'shildi!\n\n" +
                        "🚗 Raqam: %s\n" +
                        "🏷️ Marka: %s\n" +
                        "🚙 Model: %s\n" +
                        "🎨 Rang: %s\n\n" +
                        "Endi boshqa foydalanuvchilar sizni topishi mumkin!",
                savedCar.getLicensePlate(),
                savedCar.getBrand(),
                savedCar.getModel(),
                savedCar.getColor());

        sendContentMessage(bot, user.getChatId(), successMessage, null);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        showCarsMenu(bot, user.getChatId(), "🚙 Avtomobillar menyusi:");
        userService.updateUserState(user.getChatId(), UserState.CARS_MENU);
        tempCarDataService.delete(user.getChatId());
    }

    private void showCarManagementMenu(UserBot user, ParkingTelegramBot bot, Long carId) {
        Optional<Car> carOpt = carService.findById(carId);
        if (carOpt.isEmpty()) {
            showCarsMenu(bot, user.getChatId(), "❌ Avtomobil topilmadi.\n\n🚙 Avtomobillar menyusi:");
            return;
        }

        Car car = carOpt.get();
        String message = String.format(
                "🚗 Avtomobil: %s\n\n" +
                        "Quyidagi amallardan birini tanlang:",
                car.getLicensePlate());

        sendContentMessage(bot, user.getChatId(), message,
                keyboardFactory.getCarManagementKeyboard(carId));
    }

    private void showUserCars(UserBot user, ParkingTelegramBot bot) {
        List<Car> cars = carService.findByOwner(user.getChatId());

        if (cars.isEmpty()) {
            sendContentMessage(bot, user.getChatId(), "🚗 Sizda hali avtomobil ro'yxatdan o'tmagan.\n\n" +
                    "➕ Avtomobil qo'shish uchun '➕ Avtomobil Qo'shish' tugmasini bosing.", null);
            return;
        }

        StringBuilder message = new StringBuilder("🚙 Sizning avtomobillaringiz:\n\n");

        sendContentMessage(bot, user.getChatId(), message.toString(),
                keyboardFactory.getCarsListKeyboard(cars));
    }

    private void showRequestHistory(UserBot user, ParkingTelegramBot bot) {
        List<ContactRequest> sentRequests = contactService.getSentRequests(user.getChatId());
        List<ContactRequest> receivedRequests = contactService.getReceivedRequests(user.getChatId());

        StringBuilder message = new StringBuilder("📊 So'rovlar tarixi:\n\n");

        if (!sentRequests.isEmpty()) {
            message.append("📤 Yuborilgan so'rovlar:\n");
            for (ContactRequest request : sentRequests.subList(0, Math.min(5, sentRequests.size()))) {
                message.append(String.format("• %s - %s (%s)\n",
                        request.getLicensePlate(),
                        request.getReason().getDescription(),
                        request.getStatus().name()));
            }
            message.append("\n");
        }

        if (!receivedRequests.isEmpty()) {
            message.append("📥 Qabul qilingan so'rovlar:\n");
            for (ContactRequest request : receivedRequests.subList(0, Math.min(5, receivedRequests.size()))) {
                message.append(String.format("• %s - %s (%s)\n",
                        request.getLicensePlate(),
                        request.getReason().getDescription(),
                        request.getStatus().name()));
            }
        }

        if (sentRequests.isEmpty() && receivedRequests.isEmpty()) {
            message.append("📭 Hali so'rovlar yo'q.");
        }

        sendContentMessage(bot, user.getChatId(), message.toString(), null);
    }

    private void showPrivacySettings(UserBot user, ParkingTelegramBot bot) {
        String message = String.format("🔒 Maxfiylik sozlamalari:\n\n" +
                        "👤 Username ulashish: %s\n" +
                        "📱 Telefon raqami ulashish: %s\n" +
                        "🔕 Bezovta qilmaslik: %s",
                user.getShareUsername() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getSharePhoneNumber() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getDoNotDisturb() ? "✅ Yoqilgan" : "❌ O'chirilgan");

        sendContentMessage(bot, user.getChatId(), message,
                keyboardFactory.getPrivacySettingsKeyboard(user));
    }

    private void toggleDoNotDisturb(UserBot user, ParkingTelegramBot bot) {
        boolean newStatus = !user.getDoNotDisturb();
        userService.updatePrivacySettings(user.getChatId(), user.getShareUsername(), user.getSharePhoneNumber());

        String message = newStatus ?
                "🔕 Bezovta qilmaslik rejimi yoqildi. Favqulodda holatlar bundan mustasno." :
                "🔔 Bezovta qilmaslik rejimi o'chirildi.";

        sendContentMessage(bot, user.getChatId(), message, null);
    }


    private void handleWaitingEditCarBrand(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Avtomobil tahrirlash bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendContentMessage(bot, user.getChatId(),
                    "❌ Iltimos, avtomobil markasini to'liq kiriting.\n\n" +
                            "💡 Masalan: Toyota, BMW, Chevrolet\n\n" +
                            "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        tempCarDataService.updateBrand(user.getChatId(), text.trim());
        sendContentMessage(bot, user.getChatId(),
                "🚗 Avtomobil modelini kiriting:\n\n" +
                        "💡 Masalan: Camry, X5, Malibu\n\n" +
                        "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
        userService.updateUserState(user.getChatId(), UserState.WAITING_EDIT_CAR_MODEL);
    }

    private void handleWaitingEditCarModel(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Avtomobil tahrirlash bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 1) {
            sendContentMessage(bot, user.getChatId(),
                    "❌ Iltimos, avtomobil modelini kiriting.\n\n" +
                            "💡 Masalan: Camry, X5, Malibu\n\n" +
                            "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        tempCarDataService.updateModel(user.getChatId(), text.trim());
        sendContentMessage(bot, user.getChatId(),
                "🎨 Avtomobil rangini kiriting:\n\n" +
                        "💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                        "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
        userService.updateUserState(user.getChatId(), UserState.WAITING_EDIT_CAR_COLOR);
    }

    private void handleWaitingEditCarColor(UserBot user, String text, ParkingTelegramBot bot) {
        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            showMainMenu(bot, user.getChatId(), "❌ Avtomobil tahrirlash bekor qilindi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendContentMessage(bot, user.getChatId(),
                    "❌ Iltimos, avtomobil rangini kiriting.\n\n" +
                            "💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                            "🔙 Bekor qilish uchun /start buyrug'ini yuboring.", null);
            return;
        }

        Optional<TempCarData> tempDataOpt = tempCarDataService.findByChatId(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            showMainMenu(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        TempCarData tempData = tempDataOpt.get();
        Long carId = tempData.getCarId();

        if (carId == null) {
            showMainMenu(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        try {
            Car updatedCar = carService.updateCar(carId, tempData.getBrand(), tempData.getModel(), text.trim());

            String successMessage = String.format(
                    "✅ Avtomobil muvaffaqiyatli yangilandi!\n\n" +
                            "🚗 Raqam: %s\n" +
                            "🏷️ Marka: %s\n" +
                            "🚙 Model: %s\n" +
                            "🎨 Rang: %s",
                    updatedCar.getLicensePlate(),
                    updatedCar.getBrand(),
                    updatedCar.getModel(),
                    updatedCar.getColor());

            sendContentMessage(bot, user.getChatId(), successMessage, null);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            showMainMenu(bot, user.getChatId(), "🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());

        } catch (Exception e) {
            e.printStackTrace();
            showMainMenu(bot, user.getChatId(), "❌ Avtomobil yangilashda xatolik yuz berdi.\n\n🏠 Asosiy menyu:");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
        }
    }
}