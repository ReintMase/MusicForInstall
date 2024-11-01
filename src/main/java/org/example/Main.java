package org.example;

import org.example.bot.MusicBot;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class Main {
    private static final String botToken = "7957969550:AAFMrduU7fu4l48ldsr0oL6V81osvpUtTFk";
    private final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);

    public static void main(String[] args) {
        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            MusicBot musicBot = new MusicBot();

            botsApplication.registerBot(botToken, musicBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static String getBotToken() {
        return botToken;
    }
}