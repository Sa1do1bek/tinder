package com.example.parking.bot.keyboard;

import com.example.parking.entity.ContactReason;
import com.example.parking.entity.UserBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;


import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public ReplyKeyboardMarkup getPhoneRequestKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton phoneButton = new KeyboardButton("📱 Telefon Raqamini Ulash");
        phoneButton.setRequestContact(true);
        row.add(phoneButton);

        rows.add(row);
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🚗 Avtomobil Egasini Topish"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🚙 Mening Avtomobillarim"));
        row2.add(new KeyboardButton("➕ Avtomobil Qo'shish"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("📊 Tarix"));
        row3.add(new KeyboardButton("⚙️ Sozlamalar"));
        rows.add(row3);

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("📊 Service"));
        rows.add(row4);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔒 Maxfiylik Sozlamalari"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🔕 Bezovta Qilmaslik"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("🔙 Asosiy Menyu"));
        rows.add(row3);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getContactReasonKeyboard(String licensePlate) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (ContactReason reason : ContactReason.values()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(reason.getDescription());
            button.setCallbackData("reason_" + reason.name() + "_" + licensePlate.replace(" ", "_"));
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getContactOptionsKeyboard(UserBot owner, Long requestId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (Boolean.TRUE.equals(owner.getShareUsername()) && owner.getUsername() != null) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("💬 Telegram orqali yozish");
            button.setUrl("https://t.me/" + owner.getUsername());
            row.add(button);
            rows.add(row);
        }

        List<InlineKeyboardButton> phoneRow = new ArrayList<>();
        InlineKeyboardButton phoneButton = new InlineKeyboardButton();
        phoneButton.setText("📞 Telefon Raqamini So'rash");
        phoneButton.setCallbackData("contact_phone_" + requestId);
        phoneRow.add(phoneButton);
        rows.add(phoneRow);

        List<InlineKeyboardButton> resolveRow = new ArrayList<>();
        InlineKeyboardButton resolveButton = new InlineKeyboardButton();
        resolveButton.setText("✅ Muammo Hal Bo'ldi");
        resolveButton.setCallbackData("contact_resolve_" + requestId);
        resolveRow.add(resolveButton);
        rows.add(resolveRow);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getOwnerResponseKeyboard(Long requestId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton allowPhoneButton = new InlineKeyboardButton();
        allowPhoneButton.setText("📞 Telefon Raqamimni Berish");
        allowPhoneButton.setCallbackData("owner_allow_phone_" + requestId);
        row1.add(allowPhoneButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton resolveButton = new InlineKeyboardButton();
        resolveButton.setText("✅ Hal Qildim");
        resolveButton.setCallbackData("owner_resolve_" + requestId);
        row2.add(resolveButton);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getPhoneShareKeyboard(Long requesterChatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("✅ Ha, ulashaman");
        yesButton.setCallbackData("phone_share_yes_" + requesterChatId);
        row1.add(yesButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("❌ Yo'q, ulashmayman");
        noButton.setCallbackData("phone_share_no_" + requesterChatId);
        row2.add(noButton);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getPrivacySettingsKeyboard(UserBot user) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton usernameButton = new InlineKeyboardButton();
        usernameButton.setText((user.getShareUsername() ? "❌ " : "✅ ") + "Username ulashish");
        usernameButton.setCallbackData("privacy_username_" + user.getChatId());
        row1.add(usernameButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton phoneButton = new InlineKeyboardButton();
        phoneButton.setText((user.getSharePhoneNumber() ? "❌ " : "✅ ") + "Telefon ulashish");
        phoneButton.setCallbackData("privacy_phone_" + user.getChatId());
        row2.add(phoneButton);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboardMarkup getServiceKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🛡 Sug'urta"));
        row1.add(new KeyboardButton("🔧 Texnik ko'rik"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🛢 Avtomobil moyi"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("🔙 Orqaga"));
        rows.add(row3);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup getInsuranceLinksKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton insonButton = new InlineKeyboardButton();
        insonButton.setText("🛡 Kafolat sug'urta");
        insonButton.setUrl("https://online.kafolat.uz/online/r/ins/e-osgo/calc?session=9138287983306");

        InlineKeyboardButton grossButton = new InlineKeyboardButton();
        grossButton.setText("🛡 Apex sug'urta");
        grossButton.setUrl("https://epolis.aic.uz/apex/r/ins/a12020241007182522253120/home");

        InlineKeyboardButton alSKOMButton = new InlineKeyboardButton();
        alSKOMButton.setText("🛡 AlSKOM sug'urta");
        alSKOMButton.setUrl("https://alskom.uz");

        rows.add(List.of(insonButton));
        rows.add(List.of(grossButton));
        rows.add(List.of(alSKOMButton));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }


}
