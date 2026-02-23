package com.downloadvideo.infrastructure.telegram.bot;

import com.downloadvideo.infrastructure.telegram.bot.data.TelegramBotData;
import com.downloadvideo.application.telegram.handler.main.MainHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.threadpool.FixedThreadPool;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class MainBot extends TelegramWebhookBot {
    public MainBot(TelegramBotData telegramBotData,@Lazy MainHandler mainHandler) {
        super(telegramBotData.getToken());
        this.telegramBotData = telegramBotData;
        this.mainHandler = mainHandler;
    }

    private MainHandler mainHandler;
    private TelegramBotData telegramBotData;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            0L,
            java.util.concurrent.TimeUnit.MILLISECONDS,
            // очередь на 0 => вообще без очереди (жестко: либо сразу выполняется, либо отказ)
            new java.util.concurrent.SynchronousQueue<>(),
            new ThreadPoolExecutor.DiscardPolicy() // молча отбросить
    );

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mainHandler.handler(update);
            }
        });
        return null;
    }


    @Override
    public String getBotPath() {
        return telegramBotData.getPath();
    }

    @Override
    public String getBotUsername() {
        return telegramBotData.getName();
    }


}

