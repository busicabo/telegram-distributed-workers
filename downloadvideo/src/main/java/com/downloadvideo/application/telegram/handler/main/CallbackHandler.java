package com.downloadvideo.application.telegram.handler.main;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.adapters.in.telegram.dto.CallbackEvent;
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
