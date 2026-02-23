package com.downloadvideo.application.telegram.handler.main;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.adapters.in.telegram.dto.MessageEvent;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

//Наследуют классы, которые обрабатывают сообщение от пользователей.
@Service
public interface MessageHandlers {
    //Сможет ли обработать этот апдейт
    boolean isValid(String message);
    //Начать обработку апдейта
    void handle(MessageEvent event);
}
