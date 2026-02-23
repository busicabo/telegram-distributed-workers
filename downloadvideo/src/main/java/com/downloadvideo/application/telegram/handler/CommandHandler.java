package com.downloadvideo.application.telegram.handler;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.adapters.in.telegram.dto.MessageEvent;
import com.downloadvideo.application.service.SendTelegram;
import com.downloadvideo.application.telegram.handler.main.MessageHandlers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/*
Обрабатывает все команды пользователей. Админ команды - AdminCommandHandler
 */

/*
Список всех команд:
(Старт бота) /start
 */
@Service
@Slf4j
public class CommandHandler implements MessageHandlers {
    @Autowired
    private SendTelegram sendTelegram;

    //Список доступных команд
    private final List<String> commands = List.of("/start");
    @Override
    public boolean isValid(String message) {
        for(String command: commands){
            if(message.trim().startsWith(command)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(MessageEvent event) {
        Update update = event.getUpdate();
        String command = update.getMessage().getText();
        if(command.trim().startsWith("/start")){
            SendMessage message = new SendMessage(update.getMessage().getChatId().toString(),
                    "\uD83E\uDD16 Отправь ссылку на видео и я скачаю его со скоростью света! ");
            sendTelegram.sendMessage(message);
        }
    }
}
