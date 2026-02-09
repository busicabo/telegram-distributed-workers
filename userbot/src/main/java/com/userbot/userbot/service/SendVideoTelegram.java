package com.userbot.userbot.service;

import com.userbot.userbot.model.*;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
/*
Отправка видео в сам телеграм. Задача не будет закончена и поток не освободиться,
пока видео не загрузиться полностью.
*/
public class SendVideoTelegram {
    @Autowired
    private SimpleTelegramClient client;
    @Autowired
    private VideoDownloaded videoDownloaded;
    @Autowired
    private SendStatus sendStatus;
    @Autowired
    private PreviewCheckWidthAndHeightService checkPreview;
    private String path = DownloadPath.PATH;
    @Value("${bot.chat_id}")
    private Long chatId;
    public boolean videoDownloadProcess(Task task) {
        String pathVideo = "";
        try {
            try {
                pathVideo = Files.list(Path.of(path))
                        .filter(p -> p.getFileName().toString().equalsIgnoreCase(task.getVideoDataId().toString()+".mp4"))
                        .findFirst()
                        .map(Path::toString)
                        .orElseThrow();
            } catch (IOException e) {
                log.error("Ошибка при получении файла!", e);
                return false;
            }
            if(pathVideo.isEmpty()){
                log.info("Видео файл не найден. id:{}",task.getVideoDataId());
                return false;
            }
            TdApi.InputFileLocal thumbFile = new TdApi.InputFileLocal(path+"/"+task.getVideoDataId()+".jpg");
            ImageMeta imageMeta = checkPreview.getPreviewMeta(task.getVideoDataId());
            TdApi.InputThumbnail thumb = null;
            if(imageMeta!=null){
                thumb = new TdApi.InputThumbnail(thumbFile,imageMeta.width(),imageMeta.height());
            } else{
                thumb = new TdApi.InputThumbnail(thumbFile,1280,720);
            }
            TdApi.InputFileLocal inputFileLocal = new TdApi.InputFileLocal(pathVideo);
            TdApi.FormattedText formattedText = new TdApi.FormattedText(task.getVideoDataId().toString(),null);
            TdApi.InputMessageVideo inputMessageVideo = new TdApi.InputMessageVideo(
                    inputFileLocal, thumb,
                    null, task.getDuration(), 1920, 1080,
                    true, formattedText, null, false);
            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = chatId;
            sendMessage.inputMessageContent = inputMessageVideo;

            CompletableFuture<TdApi.File> completableFuture = new CompletableFuture<>();
            log.info("Начинаем отправку... {}", task.getVideoId());
            sendStatus.sendStatus(new InfoVideo(task.getVideoDataId(),"Начинаем отправку...", InfoVideoType.INFO));

            client.send(sendMessage, result -> {
                if (result.isError()) {
                    log.info("Ошибка при отправке видео: ", new RuntimeException(result.getError().message));
                    completableFuture.completeExceptionally(new RuntimeException(result.getError().message));
                    return;
                }
                TdApi.Message message = result.get();
                Long id = message.id;
                videoDownloaded.newWaitingForSending(id, completableFuture);
            });
            try {
                completableFuture.get(5, TimeUnit.MINUTES);
                log.info("Успешная отправка видео: id:{}", task.getVideoDataId());
                return true;
            } catch (TimeoutException e) {
                log.error("TimeOut! Видео слишком долго загружается либо произошла непредвиденная ошибка!");
                return false;
            } catch (Exception e) {
                log.error("Ошибка при отправке видео! ", e);
                return false;
            }

        } catch (Exception e) {
            log.error("Ошибка при отправке видео в канал! ", e);
            return false;
        }
    }
}
