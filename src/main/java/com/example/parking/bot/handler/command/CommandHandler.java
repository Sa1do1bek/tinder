package com.example.parking.bot.handler.command;

import com.example.parking.bot.ParkingTelegramBot;
import com.example.parking.bot.keyboard.KeyboardFactory;
import com.example.parking.entity.*;
import com.example.parking.service.CarService;
import com.example.parking.service.ContactService;
import com.example.parking.service.TempCarDataService;
import com.example.parking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.List;
import java.util.Optional;

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
            default:
                handleRegisteredUserCommands(user, text, bot);
        }
    }

    private void handleServiceCommands(UserBot user, String text, ParkingTelegramBot bot) {
        // Debug ma'lumotlari
        System.out.println("=== SERVICE COMMAND DEBUG ===");
        System.out.println("Qabul qilingan matn: '" + text + "'");
        System.out.println("Matn uzunligi: " + text.length());
        System.out.println("User holati: " + user.getState());

        // Har bir belgini tekshirish
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            System.out.println("Index " + i + ": '" + c + "' (Unicode: " + (int)c + ")");
        }

        // KeyboardFactory'dan kelgan tugma matnini tekshirish
        System.out.println("Kutilayotgan matnlar:");
        System.out.println("1. '🛡 Sug'urta' uzunlik: " + "🛡 Sug'urta".length());
        System.out.println("2. '🔧 Texnik ko'rik' uzunlik: " + "🔧 Texnik ko'rik".length());
        System.out.println("3. '🛢 Avtomobil moyi' uzunlik: " + "🛢 Avtomobil moyi".length());
        System.out.println("4. '🔙 Orqaga' uzunlik: " + "🔙 Orqaga".length());

        try {
            // Matnni trim qilish va solishtirish
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
                    showAvtomobilMoyi(user, bot);
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

                    // Har bir case bilan alohida solishtirish
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

    private void showAvtomobilMoyi(UserBot user, ParkingTelegramBot bot) {
        try {
            String message = """
                🛢 Avtomobil moyi haqida ma'lumot:
                
                🔹 Moy turlari:
                   • Sintetik moy (5W-30, 5W-40)
                   • Yarim sintetik moy (10W-40)
                   • Mineral moy (15W-40)
                
                🔹 Moy almashtirish muddatlari:
                   • Sintetik: 10,000-15,000 km
                   • Yarim sintetik: 7,000-10,000 km
                   • Mineral: 5,000-7,000 km
                
                🔹 Maslahatlar:
                   • Avtomobil ishlab chiqaruvchisining tavsiyalarini kuzating
                   • Sifatli moy ishlatishni unutmang
                   • Moy filtrini ham almashtiring
                
                💡 Qo'shimcha ma'lumot uchun avtomobil xizmat markazlariga murojaat qiling.
                """;

            sendMessage(bot, user.getChatId(), message, keyboardFactory.getServiceKeyboard());
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(bot, user.getChatId(), "❌ Ma'lumot yuklashda xatolik yuz berdi.",
                    keyboardFactory.getServiceKeyboard());
        }
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
            if (user.getState() == UserState.REGISTERED) {
                sendMessage(bot, chatId, "🏠 Xush kelibsiz! Quyidagi menyudan kerakli bo'limni tanlang:",
                        keyboardFactory.getMainMenuKeyboard());
            } else {
                handleNewUserState(user, "/start", bot);
            }
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
        if (!carService.isValidLicensePlate(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA");
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);
        Optional<Car> carOpt = carService.findByLicensePlate(formattedPlate);

        if (carOpt.isEmpty()) {
            sendMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil topilmadi.\n\n" +
                    "🔍 Avtomobil egasi hali botdan ro'yxatdan o'tmagan bo'lishi mumkin.\n" +
                    "💡 Ularni botdan foydalanishga taklif qiling!");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        Car car = carOpt.get();
        UserBot owner = car.getOwner();

        if (owner.getChatId().equals(user.getChatId())) {
            sendMessage(bot, user.getChatId(), "ℹ️ Bu sizning avtomobilingiz!");
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

    private void handleCarNumberInput(UserBot user, String text, ParkingTelegramBot bot) {
        if (!carService.isValidLicensePlate(text)) {
            sendMessage(bot, user.getChatId(), "❌ Noto'g'ri format! Iltimos, to'g'ri formatda kiriting:\n\n" +
                    "📝 Format: 01 A 123 AA\n" +
                    "💡 Masalan: 01 A 123 AA yoki 01A123AA");
            return;
        }

        String formattedPlate = carService.formatLicensePlate(text);

        if (carService.isLicensePlateExists(formattedPlate)) {
            sendMessage(bot, user.getChatId(), "❌ Bu raqamli avtomobil allaqachon ro'yxatdan o'tgan!");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            tempCarDataService.delete(user.getChatId());
            return;
        }

        tempCarDataService.updateLicensePlate(user.getChatId(), formattedPlate);
        sendMessage(bot, user.getChatId(), "🏷️ Avtomobil markasini kiriting:\n\n💡 Masalan: Toyota, BMW, Chevrolet");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_BRAND);
    }

    private void handleCarBrandInput(UserBot user, String text, ParkingTelegramBot bot) {
        tempCarDataService.updateBrand(user.getChatId(), text);
        sendMessage(bot, user.getChatId(), "🚗 Avtomobil modelini kiriting:\n\n💡 Masalan: Camry, X5, Malibu");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_MODEL);
    }

    private void handleCarModelInput(UserBot user, String text, ParkingTelegramBot bot) {
        tempCarDataService.updateModel(user.getChatId(), text);
        sendMessage(bot, user.getChatId(), "🎨 Avtomobil rangini kiriting:\n\n💡 Masalan: Oq, Qora, Ko'k, Qizil");
        userService.updateUserState(user.getChatId(), UserState.WAITING_CAR_COLOR);
    }

    private void handleCarColorInput(UserBot user, String text, ParkingTelegramBot bot) {
        Optional<TempCarData> tempDataOpt = tempCarDataService.findByChatId(user.getChatId());
        if (tempDataOpt.isEmpty()) {
            sendMessage(bot, user.getChatId(), "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.");
            userService.updateUserState(user.getChatId(), UserState.REGISTERED);
            return;
        }

        TempCarData tempData = tempDataOpt.get();
        tempCarDataService.updateColor(user.getChatId(), text);

        Car savedCar = carService.addCar(user, tempData.getLicensePlate(), tempData.getBrand(), tempData.getModel(), text);

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
