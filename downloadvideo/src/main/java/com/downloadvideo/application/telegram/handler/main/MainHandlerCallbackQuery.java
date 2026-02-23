package com.downloadvideo.application.telegram.handler.main;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.adapters.in.telegram.dto.CallbackEvent;
import com.downloadvideo.application.service.SendTelegram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/*
При нажатии на Callback кнопку выбирает класс, который сможет обработать это событие,
а точнее вернет true при вызове isValid. Класс должен наследоваться от интерфейса CallbackHandler
и быть в контексте бина.
 */
@Slf4j
@Service
public class MainHandlerCallbackQuery {
    @Autowired
    private List<CallbackHandler> callbacks;
    @Autowired
    private SendTelegram sendTelegram;

    public void start(CallbackEvent event){
        for(CallbackHandler callback: callbacks){
            if(callback.isValid(event.getUpdate().getCallbackQuery())){
                callback.handle(event);
                return;
            }
        }
        SendMessage message = new SendMessage(String.valueOf(event.getUpdate().getCallbackQuery().getMessage().getChatId()),
                "❗ Данная кнопка не найдена!");
        sendTelegram.sendMessage(message);
    }

    @EventListener
    public void listen(CallbackEvent event){
        start(event);
    }
}
