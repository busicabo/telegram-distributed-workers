package com.downloadvideo.service.handler.main;

import com.downloadvideo.config.bot.MainBot;
import com.downloadvideo.model.event.CallbackEvent;
import com.downloadvideo.model.event.MessageEvent;
import com.downloadvideo.service.SendTelegram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/*
Обрабатывает АБСОЛЮТНО все сообщения которые пишет пользователь, включая как
обычные сообщение так и команды. Обработает это сообщение тот класс, который наследуеться от
MessageHandlers, будет в контексте бина и вернет true при вызове метода isValid.
 */
@Slf4j
@Service
public class MainHandlerMessage {
    @Autowired
    private MainBot bot;
    @Autowired
    private List<MessageHandlers> handlers;
    @Autowired
    private SendTelegram sendTelegram;

    public void start(MessageEvent event) {
        for(MessageHandlers hand: handlers){
            if(hand.isValid(event.getUpdate().getMessage().getText())){
                hand.handle(event);
                return;
            }
        }
        SendMessage sendMessage =new SendMessage();
        sendMessage.setChatId(event.getUpdate().getMessage().getChatId());
        sendMessage.setText("❗ Пожалуйста введите ссылку или команду!");
        sendTelegram.sendMessage(sendMessage);
    }

    @EventListener
    public void listen(MessageEvent event){
        start(event);
    }
}
