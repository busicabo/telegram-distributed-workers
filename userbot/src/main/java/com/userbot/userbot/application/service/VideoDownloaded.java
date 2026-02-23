package com.userbot.userbot.application.service;

import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Service
/*
Телеграм отправляет видео асинхронно, так что нужно отследить когда оно
будет загружено(придет апдейт). Этот класс помогает это реализовать.
 */
public class VideoDownloaded {
    @Autowired
    private SimpleTelegramClient client;
    private final Map<Long, CompletableFuture<TdApi.File>> map = new ConcurrentHashMap<>();

    public void newWaitingForSending(Long id,CompletableFuture<TdApi.File> completableFuture){
        map.put(id,completableFuture);
    }
    public CompletableFuture<TdApi.File> getCompletableFuture(Long id){
        return map.get(id);
    }
    public void delete(Long id){
        map.remove(id);
    }
}
