package com.downloadvideo.service;

import com.downloadvideo.config.bot.MainBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

// Единый центр отправки сообщений любого типа пользователям.
@Service
@Slf4j
public class SendTelegram {
    @Autowired
    private MainBot bot;

    public Message sendMessage(SendMessage sendMessage){
        try {
            return bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            try {
                return bot.execute(sendMessage);
            } catch (TelegramApiException ex) {
                log.error("Не удалось отправить сообщение в телеграмм! ",e);
                return null;
            }
        }
    }

    public Serializable editMessage(EditMessageText editMessageText){
        try {
            return bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            try {
                return bot.execute(editMessageText);
            } catch (TelegramApiException ex) {
                log.error("Не удалось отредактировать сообщение в телеграмм! error: {}",e.getMessage());
                return null;
            }
        }
    }

    public Message sendVideo(SendVideo sendVideo){
        try {
            return bot.execute(sendVideo);
        } catch (TelegramApiException e) {
            try {
                return bot.execute(sendVideo);
            } catch (TelegramApiException ex) {
                log.error("Не удалось отправить видео в телеграмм! ",e);
                return null;
            }
        }
    }

    public boolean deleteMessage(DeleteMessage deleteMessage){
        try {
            return bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            try {
                return bot.execute(deleteMessage);
            } catch (TelegramApiException ex) {
                log.error("Не удалось удалить сообщение в телеграмм! ",e);
                return false;
            }
        }
    }
}
