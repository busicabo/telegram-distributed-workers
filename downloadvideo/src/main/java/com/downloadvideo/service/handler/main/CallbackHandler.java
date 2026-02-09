package com.downloadvideo.service.handler.main;

import com.downloadvideo.config.bot.MainBot;
import com.downloadvideo.model.event.CallbackEvent;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

//Наследуют классы, которые обрабатывают нажатие пользователем на кнопку.
@Service
public interface CallbackHandler {
    //Сможет ли обработать этот апдейт
    boolean isValid(CallbackQuery callbackQuery);
    //Начать обработку апдейта
    void handle(CallbackEvent event);
}
