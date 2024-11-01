package org.example.bot;

import org.example.Main;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.HashSet;

public class MusicBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient = new OkHttpTelegramClient(Main.getBotToken());
    private final Set<Long> usersWithKeyboard = new HashSet<>();

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Отправляем клавиатуру только если это первое сообщение
            sendReplyKeyboardMessage(chatId);

            // Проверяем, является ли сообщение ссылкой
            File audioFile;
            try {
                if (messageText.contains("youtube.com") || messageText.contains("youtu.be")) {
                    if (messageText.contains("&list")) {
                        int index = messageText.indexOf("&list");
                        messageText = messageText.substring(0, index);
                    }

                    SendMessage confirmationMessage = new SendMessage(chatId.toString(), "Принято, ожидайте... ⏳");
                    telegramClient.execute(confirmationMessage);

                    audioFile = downloadAndConvertToMp3(messageText, "output_youtube.mp3");
                } else if (messageText.contains("tiktok.com")) {
                    SendMessage confirmationMessage = new SendMessage(chatId.toString(), "Принято, ожидайте... ⏳");
                    telegramClient.execute(confirmationMessage);

                    audioFile = downloadAndConvertToMp3(messageText, "output_tiktok.mp3");
                } else if (messageText.contains("soundcloud.com")) {
                    SendMessage confirmationMessage = new SendMessage(chatId.toString(), "Принято, ожидайте... ⏳");
                    telegramClient.execute(confirmationMessage);

                    audioFile = downloadAndConvertToMp3(messageText, "output_soundcloud.mp3");
                } else if (messageText.contains("on.soundcloud.com")) {
                    // Отправляем сообщение с подтверждением
                    SendMessage confirmationMessage = new SendMessage(chatId.toString(), "Принято, ожидайте... ⏳");
                    telegramClient.execute(confirmationMessage);

                    audioFile = downloadAndConvertToMp3(messageText, "output_soundcloud_short.mp3");
                } else {
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "Отправьте ссылку на видео YouTube, TikTok или SoundCloud.");
                    telegramClient.execute(sendMessage);
                    return;
                }

                sendAudio(chatId, audioFile);

                // Удаляем файл после отправки
                if (audioFile.exists()) {
                    audioFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SendMessage sendMessage = new SendMessage(chatId.toString(), "Произошла ошибка при загрузке аудио.");
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void sendReplyKeyboardMessage(Long chatId) {
        if (!usersWithKeyboard.contains(chatId)) {
            usersWithKeyboard.add(chatId);

            KeyboardButton button1 = new KeyboardButton("Кнопка 1");
            KeyboardButton button2 = new KeyboardButton("Кнопка 2");
            KeyboardButton button3 = new KeyboardButton("Кнопка 3");

            KeyboardRow row1 = new KeyboardRow();
            row1.add(button1);
            row1.add(button2);

            KeyboardRow row2 = new KeyboardRow();
            row2.add(button3);

            ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                    .keyboard(List.of(row1, row2))
                    .resizeKeyboard(true)
                    .selective(true)
                    .oneTimeKeyboard(false)
                    .build();

            SendMessage message = new SendMessage(chatId.toString(), "Чем могу быть полезен? Нажмите на кнопку помощи если не знаете что делать!");
            message.setReplyMarkup(keyboardMarkup);

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private File downloadAndConvertToMp3(String url, String outputFileName) throws IOException, InterruptedException {
        if (url.contains("&list")) {
            int index = url.indexOf("&list");
            url = url.substring(0, index);
        }

        ProcessBuilder pb = new ProcessBuilder("C:\\yt-dlp\\yt-dlp.exe", "-x", "--audio-format", "mp3", "--ffmpeg-location", "C:\\yt-dlp\\ffmpeg", "-o", outputFileName, url);

        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Ошибка при выполнении yt-dlp. Код выхода: " + exitCode);
        }

        File mp3File = new File(outputFileName);
        if (!mp3File.exists()) {
            throw new IOException("Файл " + outputFileName + " не был создан.");
        }

        return mp3File;
    }

    private void sendAudio(Long chatId, File audioFile) {
        SendAudio sendAudio = new SendAudio(chatId.toString(), new InputFile(audioFile));

        try {
            telegramClient.execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);

        try {
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
