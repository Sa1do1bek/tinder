package com.example.tinder;

import com.example.tinder.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@SpringBootApplication
@ComponentScan({"com.example.tinder", "com.example.parking"})
@EntityScan({"com.example.tinder.entity", "com.example.parking.entity"})
@EnableScheduling
@EnableJpaRepositories({"com.example.tinder.repository", "com.example.parking.repository"})
@RequiredArgsConstructor
public class TinderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TinderApplication.class, args);
    }

    private final TelegramBot telegramBot;

    @Override
    public void run(String... args) throws Exception {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            System.out.println("✅ Parking Bot muvaffaqiyatli ishga tushdi!");
            System.out.println("🤖 Bot username: @" + telegramBot.getBotUsername());
            System.out.println("📱 Telegram'da /start buyrug'ini yuboring");
        } catch (TelegramApiException e) {
            System.err.println("❌ Bot ro'yxatdan o'tishda xatolik:");
            System.err.println("🔑 Bot token tekshiring: " + telegramBot.getBotToken());
            System.err.println("📝 Xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }
}