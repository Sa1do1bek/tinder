package com.example.parking.bot.handler.command;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.keyboard.KeyboardFactory;
import com.example.parking.entity.*;
import com.example.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Component
public class CommandHandler {

    @Autowired
    @Qualifier("parkingService")
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

    public void handleTextMessage(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();
        String text = message.getText();

        Optional<UserBot> userOpt = userService.findByChatId(chatId);

        if (text.equals("/start")) {
            handleStartCommand(message, bot);
            return;
        }

        if (userOpt.isEmpty()) {
            sendMessage(bot, chatId, "Iltimos, avval /start buyrug'ini bosing.");
            return;
        }

        UserBot user = userOpt.get();

        switch (user.getState()) {
            case NEW:
                handleNewUserState(user, text, bot);
                break;
            case WAITING_PHONE:
                handleWaitingPhoneState(user, text, bot);
                break;
            case REGISTERED:
                handleRegisteredUserCommands(user, text, bot);
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
            case SERVICE:
                handleServiceCommands(user,text,bot);
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
            default:
                handleRegisteredUserCommands(user, text, bot);
        }
    }

    private void handleServiceCommands(UserBot user, String text, ParkingTelegramBot bot) {
        System.out.println("=== SERVICE COMMAND DEBUG ===");
        System.out.println("Qabul qilingan matn: '" + text + "'");
        System.out.println("Matn uzunligi: " + text.length());
        System.out.println("UserBot holati: " + user.getState());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            System.out.println("Index " + i + ": '" + c + "' (Unicode: " + (int)c + ")");
        }

        System.out.println("Kutilayotgan matnlar:");
        System.out.println("1. '🛡 Sug'urta' uzunlik: " + "🛡 Sug'urta".length());
        System.out.println("2. '🔧 Texnik ko'rik' uzunlik: " + "🔧 Texnik ko'rik".length());
        System.out.println("3. '🛢 Avtomobil moyi' uzunlik: " + "🛢 Avtomobil moyi".length());
        System.out.println("4. '🔙 Orqaga' uzunlik: " + "🔙 Orqaga".length());

        try {
            String cleanText = text.trim();
            System.out.println("Tozalangan matn: '" + cleanText + "'");

            switch (cleanText) {
                case "🛡 Sug'urta":
                    System.out.println("✅ Sug'urta case'i ishladi");
                    showSugurta(user, bot);
                    break;
                case "🔧 Texnik ko'rik":
                    System.out.println("✅ Texnik ko'rik case'i ishladi");
                    showTexnikKorik(user, bot);
                    break;
                case "🛢 Avtomobil moyi":
                    System.out.println("✅ Avtomobil moyi case'i ishladi");
                    startOilChangeProcess(user, bot);
                    break;
                case "🔙 Orqaga":
                    System.out.println("✅ Orqaga case'i ishladi");
                    sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                            keyboardFactory.getMainMenuKeyboard());
                    userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                    break;
                default:
                    System.out.println("❌ Default case'ga tushdi");
                    System.out.println("Qabul qilingan: '" + cleanText + "'");

                    System.out.println("Sug'urta bilan solishtirish: " + cleanText.equals("🛡 Sug'urta"));
                    System.out.println("Texnik ko'rik bilan solishtirish: " + cleanText.equals("🔧 Texnik ko'rik"));
                    System.out.println("Avtomobil moyi bilan solishtirish: " + cleanText.equals("🛢 Avtomobil moyi"));
                    System.out.println("Orqaga bilan solishtirish: " + cleanText.equals("🔙 Orqaga"));

                    sendMessage(bot, user.getChatId(), "❓ Noma'lum buyruq. Iltimos, menyudan tanlang:",
                            keyboardFactory.getServiceKeyboard());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
        }
        System.out.println("=== DEBUG TUGADI ===");
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

            sendMessage(bot, user.getChatId(), message);
            userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_LAST_DATE);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.",
                    keyboardFactory.getServiceKeyboard());
        }
    }

    private void handleOilWaitingLastDate(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidDate(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri sana formati! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "📅 Format: dd.MM.yyyy\n" +
                    "💡 Masalan: 15.06.2025\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
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

        sendMessage(bot, user.getChatId(), message);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_LAST_MILEAGE);
    }

    private void handleOilWaitingLastMileage(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidMileage(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri probeg formati! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "🚗 Faqat raqam kiriting\n" +
                    "💡 Masalan: 125000\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
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

        sendMessage(bot, user.getChatId(), message);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_BRAND);
    }

    private void handleOilWaitingBrand(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendMessage(bot, user.getChatId(), "❌ Iltimos, moy markasini to'liq kiriting.\n\n" +
                    "💡 Masalan: ZIC X9, Castrol GTX, Shell Helix\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
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

        sendMessage(bot, user.getChatId(), message);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_RECOMMENDED_KM);
    }

    private void handleOilWaitingRecommendedKm(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidRecommendedKm(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri interval! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "📏 1000 dan 50000 gacha\n" +
                    "💡 Masalan: 8000, 10000, 15000\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
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

        sendMessage(bot, user.getChatId(), message);
        userService.updateUserState(user.getChatId(), UserState.OIL_WAITING_DAILY_DISTANCE);
    }

    private void handleOilWaitingDailyDistance(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Moy ma'lumotlari kiritish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            oilChangeService.deleteTempData(user.getChatId());
            return;
        }

        if (!oilChangeService.isValidDailyDistance(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri kunlik masofa! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "🚗 1 dan 1000 gacha\n" +
                    "💡 Masalan: 40, 50, 100\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
            return;
        }

        int dailyDistance = oilChangeService.parseMileage(text);
        oilChangeService.updateTempDailyDistance(user.getChatId(), dailyDistance);

        Optional<TempOilChangeData> tempDataOpt = oilChangeService.getTempData(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            sendMessage(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
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

        sendMessage(bot, user.getChatId(), resultMessage, keyboardFactory.getMainMenuKeyboard());
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

            sendMessage(bot, user.getChatId(), message, keyboardFactory.getServiceKeyboard());
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(bot, user.getChatId(), "❌ Ma'lumot yuklashda xatolik yuz berdi.",
                    keyboardFactory.getServiceKeyboard());
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
            sendMessageWithInlineKeyboard(bot, user.getChatId(), text, keyboard);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(bot, user.getChatId(), "❌ Ma'lumot yuklashda xatolik yuz berdi.",
                    keyboardFactory.getServiceKeyboard());
        }
    }


    public void handleContactMessage(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();
        Optional<UserBot> userOpt = userService.findByChatId(chatId);

        if (userOpt.isEmpty()) {
            sendMessage(bot, chatId, "Iltimos, avval /start buyrug'ini bosing.");
            return;
        }

        UserBot user = userOpt.get();
        if (user.getState() == UserState.WAITING_PHONE) {
            String phoneNumber = message.getContact().getPhoneNumber();
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }

            if (userService.isPhoneNumberExists(phoneNumber)) {
                sendMessage(bot, chatId, "Bu telefon raqami allaqachon ro'yxatdan o'tgan. Boshqa raqam kiriting.");
                return;
            }

            userService.updatePhoneNumber(chatId, phoneNumber);
            sendMessage(bot, chatId, "🎉 Tabriklaymiz! Siz muvaffaqiyatli ro'yxatdan o'tdingiz.\n\n" +
                            "Endi siz avtomobil egalarini topishingiz va o'z avtomobillaringizni qo'shishingiz mumkin.",
                    keyboardFactory.getMainMenuKeyboard());
        }
    }

    private void handleStartCommand(Message message, ParkingTelegramBot bot) {
        Long chatId = message.getChatId();
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
                    "Davom etish uchun telefon raqamingizni ulashing:";

            sendMessage(bot, chatId, welcomeText, keyboardFactory.getPhoneRequestKeyboard());
            userService.updateUserState(chatId, UserState.WAITING_PHONE);
        } else {
            UserBot user = userOpt.get();

            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() &&
                    user.getState() == UserState.REGISTERED) {
                sendMessage(bot, chatId, "🏠 Xush kelibsiz! Quyidagi menyudan kerakli bo'limni tanlang:",
                        keyboardFactory.getMainMenuKeyboard());
            } else {
                if (user.getState() == UserState.WAITING_PHONE || user.getPhoneNumber() == null) {
                    String welcomeText = "📱 Ro'yxatdan o'tishni yakunlash uchun telefon raqamingizni ulashing:";
                    sendMessage(bot, chatId, welcomeText, keyboardFactory.getPhoneRequestKeyboard());
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
                sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                        keyboardFactory.getMainMenuKeyboard());
                break;
            case WAITING_PHONE:
                sendMessage(bot, user.getChatId(), "📱 Telefon raqamingizni ulash tugmasini bosing.",
                        keyboardFactory.getPhoneRequestKeyboard());
                break;
            case SETTINGS_MENU:
                sendMessage(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:",
                        keyboardFactory.getSettingsKeyboard());
                break;
            case SERVICE:
                sendMessage(bot, user.getChatId(), "Quyidagi xizmatlardan birini tanlang:",
                        keyboardFactory.getServiceKeyboard());
                break;
            default:
                sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                        keyboardFactory.getMainMenuKeyboard());
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
        }
    }

    private void handleNewUserState(UserBot user, String text, ParkingTelegramBot bot) {
        String welcomeText = "📱 Ro'yxatdan o'tish uchun telefon raqamingizni ulashing:";
        sendMessage(bot, user.getChatId(), welcomeText, keyboardFactory.getPhoneRequestKeyboard());
        userService.updateUserState(user.getChatId(), UserState.WAITING_PHONE);
    }

    private void handleWaitingPhoneState(UserBot user, String text, ParkingTelegramBot bot) {
        sendMessage(bot, user.getChatId(), "📱 Iltimos, telefon raqamingizni ulash tugmasini bosing.",
                keyboardFactory.getPhoneRequestKeyboard());
    }

    private void handleRegisteredUserCommands(UserBot user, String text, ParkingTelegramBot bot) {
        switch (text) {
            case "🚗 Avtomobil Egasini Topish":
                sendMessage(bot, user.getChatId(), "🔍 Qidirilayotgan avtomobilning davlat raqamini kiriting:\n\n" +
                        "📝 Format: 01 A 123 AA\n" +
                        "💡 Masalan: 01 A 123 AA yoki 01A123AA");
                userService.updateUserState(user.getChatId(), UserState.WAITING_LICENSE_PLATE);
                break;

            case "🚙 Mening Avtomobillarim":
                showUserCars(user, bot);
                break;

            case "➕ Avtomobil Qo'shish":
                tempCarDataService.createOrUpdate(user.getChatId());
                sendMessage(bot, user.getChatId(), "🚗 Avtomobilingizning davlat raqamini kiriting:\n\n" +
                        "📝 Format: 01 A 123 AA\n" +
                        "💡 Masalan: 01 A 123 AA yoki 01A123AA");
                userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_NUMBER);
                break;

            case "📊 Tarix":
                showRequestHistory(user, bot);
                break;

            case "⚙️ Sozlamalar":
                sendMessage(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:",
                        keyboardFactory.getSettingsKeyboard());
                userService.updateUserState(user.getChatId(), UserState.SETTINGS_MENU);
                break;

            case "🔙 Asosiy Menyu":
                sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                        keyboardFactory.getMainMenuKeyboard());
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
            case "📊 Service":
                sendMessage(bot, user.getChatId(), "Quyidagi xizmatlardan birini tanlang:",
                        keyboardFactory.getServiceKeyboard());
                userService.updateUserState(user.getChatId(), UserState.SERVICE);
                break;

            default:
                sendMessage(bot, user.getChatId(), "❓ Noma'lum buyruq. Iltimos, menyudan tanlang.",
                        keyboardFactory.getMainMenuKeyboard());
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
                sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                        keyboardFactory.getMainMenuKeyboard());
                userService.updateUserState(user.getChatId(), UserState.REGISTERED);
                break;
            default:
                sendMessage(bot, user.getChatId(), "⚙️ Sozlamalar menyusi:",
                        keyboardFactory.getSettingsKeyboard());
        }
    }

    private void handleLicensePlateInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu")) {
            sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        if (text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        if (!carService.isValidLicensePlate(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, avtomobil raqamini to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.");
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);
        Optional<Car> carOpt = carService.findByLicensePlate(formattedPlate);

        if (carOpt.isEmpty()) {
            sendMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil topilmadi.\n\n" +
                    "🔍 Avtomobil egasi hali botdan ro'yxatdan o'tmagan bo'lishi mumkin.\n" +
                    "💡 Ularni botdan foydalanishga taklif qiling!\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        Car car = carOpt.get();
        UserBot owner = car.getOwner();

        if (owner.getChatId().equals(user.getChatId())) {
            sendMessage(bot, user.getChatId(), "ℹ️ Bu sizning avtomobilingiz!\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        String message = String.format("✅ Avtomobil topildi!\n\n🚗 Raqam: %s\n🏷️ Marka: %s\n🎨 Rang: %s\n\n📝 Murojaat sababini tanlang:",
                formattedPlate,
                car.getBrand() != null ? car.getBrand() : "Ko'rsatilmagan",
                car.getColor() != null ? car.getColor() : "Ko'rsatilmagan");
        sendMessageWithInlineKeyboard(bot, user.getChatId(), message,
                keyboardFactory.getContactReasonKeyboard(formattedPlate));

        userService.updateUserState(user.getChatId(), UserState.REGISTERED);
    }

    private boolean isMainMenuButton(String text) {
        return text.equals("🚗 Avtomobil Egasini Topish") ||
                text.equals("🚙 Mening Avtomobillarim") ||
                text.equals("➕ Avtomobil Qo'shish") ||
                text.equals("📊 Tarix") ||
                text.equals("⚙️ Sozlamalar") ||
                text.equals("📊 Service");
    }

    private void handleCarNumberInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (!carService.isValidLicensePlate(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, avtomobil raqamini to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);

        if (carService.isLicensePlateExists(formattedPlate)) {
            sendMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil allaqachon ro'yxatdan o'tgan!\n\n" +
                    "🔙 Asosiy menyuga qaytish uchun /start buyrug'ini yuboring.");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        tempCarDataService.updateLicensePlate(user.getChatId(), formattedPlate);
        sendMessage(bot, user.getChatId(), "🏷️ Avtomobil markasini kiriting:\n\n💡 Masalan: Toyota, BMW, Chevrolet\n\n" +
                "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_BRAND);
    }

    private void handleCarBrandInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil markasini to'liq kiriting.\n\n" +
                    "💡 Masalan: Toyota, BMW, Chevrolet\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
            return;
        }

        tempCarDataService.updateBrand(user.getChatId(), text.trim());
        sendMessage(bot, user.getChatId(), "🚗 Avtomobil modelini kiriting:\n\n💡 Masalan: Camry, X5, Malibu\n\n" +
                "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_MODEL);
    }

    private void handleCarModelInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 1) {
            sendMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil modelini kiriting.\n\n" +
                    "💡 Masalan: Camry, X5, Malibu\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
            return;
        }

        tempCarDataService.updateModel(user.getChatId(), text.trim());
        sendMessage(bot, user.getChatId(), "🎨 Avtomobil rangini kiriting:\n\n💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_COLOR);
    }

    private void handleCarColorInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (isMainMenuButton(text)) {
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            handleRegisteredUserCommands(user, text, bot);
            return;
        }

        if (text.equals("🔙 Orqaga") || text.equals("❌ Bekor qilish") || text.equals("🏠 Asosiy Menyu") || text.equals("/start")) {
            sendMessage(bot, user.getChatId(), "❌ Avtomobil qo'shish bekor qilindi.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        if (text.trim().length() < 2) {
            sendMessage(bot, user.getChatId(), "❌ Iltimos, avtomobil rangini kiriting.\n\n" +
                    "💡 Masalan: Oq, Qora, Ko'k, Qizil\n\n" +
                    "🔙 Bekor qilish uchun /start buyrug'ini yuboring.");
            return;
        }

        Optional<TempCarData> tempDataOpt = tempCarDataService.findByChatId(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            sendMessage(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.\n\n🏠 Asosiy menyu:",
                    keyboardFactory.getMainMenuKeyboard());
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
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

        sendMessage(bot, user.getChatId(), successMessage, keyboardFactory.getMainMenuKeyboard());
        userService.updateUserState(user.getChatId(), UserState.REGISTERED);
        tempCarDataService.delete(user.getChatId());
    }

    private void showUserCars(UserBot user, ParkingTelegramBot bot) {
        List<Car> cars = carService.findByOwner(user.getChatId());

        if (cars.isEmpty()) {
            sendMessage(bot, user.getChatId(), "🚗 Sizda hali avtomobil ro'yxatdan o'tmagan.\n\n" +
                    "➕ Avtomobil qo'shish uchun '➕ Avtomobil Qo'shish' tugmasini bosing.");
            return;
        }

        StringBuilder message = new StringBuilder("🚙 Sizning avtomobillaringiz:\n\n");
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            message.append(String.format("%d. 🚗 %s\n   🏷️ %s %s\n   🎨 %s\n\n",
                    i + 1,
                    car.getLicensePlate(),
                    car.getBrand() != null ? car.getBrand() : "Noma'lum",
                    car.getModel() != null ? car.getModel() : "",
                    car.getColor() != null ? car.getColor() : "Noma'lum"));
        }

        sendMessage(bot, user.getChatId(), message.toString());
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

        sendMessage(bot, user.getChatId(), message.toString());
    }

    private void showPrivacySettings(UserBot user, ParkingTelegramBot bot) {
        String message = String.format("🔒 Maxfiylik sozlamalari:\n\n" +
                        "👤 Username ulashish: %s\n" +
                        "📱 Telefon raqami ulashish: %s\n" +
                        "🔕 Bezovta qilmaslik: %s",
                user.getShareUsername() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getSharePhoneNumber() ? "✅ Yoqilgan" : "❌ O'chirilgan",
                user.getDoNotDisturb() ? "✅ Yoqilgan" : "❌ O'chirilgan");

        sendMessageWithInlineKeyboard(bot, user.getChatId(), message,
                keyboardFactory.getPrivacySettingsKeyboard(user));
    }

    private void toggleDoNotDisturb(UserBot user, ParkingTelegramBot bot) {
        boolean newStatus = !user.getDoNotDisturb();
        userService.updatePrivacySettings(user.getChatId(), user.getShareUsername(), user.getSharePhoneNumber());

        String message = newStatus ?
                "🔕 Bezovta qilmaslik rejimi yoqildi. Favqulodda holatlar bundan mustasno." :
                "🔔 Bezovta qilmaslik rejimi o'chirildi.";

        sendMessage(bot, user.getChatId(), message);
    }

    private void sendMessage(ParkingTelegramBot bot, Long chatId, String text) {
        sendMessage(bot, chatId, text, (ReplyKeyboardMarkup) null);
    }

    private void sendMessage(ParkingTelegramBot bot, Long chatId, String text, ReplyKeyboardMarkup keyboard) {
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

    private void sendMessageWithInlineKeyboard(ParkingTelegramBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
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
}
