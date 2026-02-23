package com.userbot.userbot.adapters.in.telegram.events;

import com.userbot.userbot.application.service.VideoDownloaded;
import it.tdlight.jni.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
//Когда видео загрузиться в телеграм, этот слушатель поймает это событие и отправит на обработку.
public class UpdateMessageSendEvent {
    @Autowired
    private VideoDownloaded videoDownloaded;
    @EventListener
    public void listen(TdApi.UpdateMessageSendSucceeded update){
        CompletableFuture<TdApi.File> completableFuture = videoDownloaded.getCompletableFuture(update.oldMessageId);
        if(completableFuture!=null){
            completableFuture.complete(null);
            videoDownloaded.delete(update.oldMessageId);
        }
    }
    @EventListener
    public void listen(TdApi.UpdateMessageSendFailed update){
        CompletableFuture<TdApi.File> completableFuture = videoDownloaded.getCompletableFuture(update.oldMessageId);
        if(completableFuture!=null){
            completableFuture.completeExceptionally(new RuntimeException(update.error.message));
            videoDownloaded.delete(update.oldMessageId);
        }
    }
}
