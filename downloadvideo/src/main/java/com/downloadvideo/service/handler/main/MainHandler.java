package com.downloadvideo.service.handler.main;

import com.downloadvideo.config.bot.MainBot;
import com.downloadvideo.config.bot.data.TelegramBotData;
import com.downloadvideo.model.event.CallbackEvent;
import com.downloadvideo.model.event.ChannelPostEvent;
import com.downloadvideo.model.event.MessageEvent;
import com.downloadvideo.service.SendTelegram;
import com.downloadvideo.service.handler.HandlerSendVideoToUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Здесь обрабатываеються АБСОЛЮТНО все апдейты которые приходят и распределяются через
отправку события(ApplicationEventPublisher) конертного типа апдейта(Сообщение в боте,
нажали на кнопку, новый пост в группе, где бот админ и т.п.
 */
@Slf4j
@Service
public class MainHandler {
    public MainHandler(ApplicationEventPublisher publisher, SendTelegram sendTelegram){
        this.publisher=publisher;
        this.sendTelegram=sendTelegram;
    }
    private SendTelegram sendTelegram;
    //Иногда телеграм присылает 2 одинаковых апдейта(с разными айди) и нужно устранить дубликаты.
    private final Set<String> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ApplicationEventPublisher publisher;

    public void handler(Update update){
        if(update.hasChannelPost()){
            Message message = update.getChannelPost();
            if(message.hasVideo()){
                String key = message.getChatId() + ":" + message.getMessageId();
                if (!seen.add(key)) {
                    log.info("Дубликат события по сообщению {}, пропускаю", key);
                    return;
                }
                log.info("Пришло новое видео!");
                Video video = message.getVideo();
                String taskId = message.getCaption();
                String fileId = video.getFileId();
                publisher.publishEvent(new ChannelPostEvent(Long.parseLong(taskId),fileId,true));
                return;
            }

        }
        if(update.hasCallbackQuery()){
            publisher.publishEvent(new CallbackEvent(update));
            return;
        }
        if(update.hasMessage()){
            Message message = update.getMessage();
            if(message.hasText()){
                publisher.publishEvent(new MessageEvent(update));
                return;
            }else {
                SendMessage sendMessage = new SendMessage(String.valueOf(update.getMessage().getChatId()),"Введите корректное сообщение!");
                sendTelegram.sendMessage(sendMessage);
            }
        }
        log.debug("Некорректный тип Update: {}",update);
    }

    //каждые 5 минут отчистка сэта с сохранеными апдейта во избежания дубликата.
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void clearSeenCache() {
        seen.clear();
        log.info("Кэш сохраненных сообщений с видео отчищен!");
    }
}
