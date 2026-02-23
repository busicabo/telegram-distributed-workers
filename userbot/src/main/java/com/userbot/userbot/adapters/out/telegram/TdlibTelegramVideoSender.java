package com.userbot.userbot.adapters.out.telegram;

import com.userbot.userbot.application.port.out.StatusPublisherPort;
import com.userbot.userbot.application.port.out.TelegramVideoSenderPort;
import com.userbot.userbot.domain.model.*;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
/*
Отправка видео в сам телеграм. Задача не будет закончена и поток не освободиться,
пока видео не загрузиться полностью.
*/
public class TdlibTelegramVideoSender implements TelegramVideoSenderPort {

    private final SimpleTelegramClient client;
    private final VideoDownloaded videoDownloaded;
    private final StatusPublisherPort statusPublisher;
    private final PreviewCheckWidthAndHeightService checkPreview;

    @Value("${bot.chat_id}")
    private Long chatId;

    private final String path = DownloadPath.PATH;

    @Override
    public boolean sendVideo(Task task) {
        String pathVideo;
        try {
            pathVideo = Files.list(Path.of(path))
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(task.getVideoDataId() + ".mp4"))
                    .findFirst()
                    .map(Path::toString)
                    .orElseThrow();
        } catch (IOException e) {
            log.error("Ошибка при получении файла!", e);
            return false;
        } catch (Exception e) {
            log.info("Видео файл не найден. id:{}", task.getVideoDataId());
            return false;
        }

        try {
            TdApi.InputFileLocal thumbFile = new TdApi.InputFileLocal(path + "/" + task.getVideoDataId() + ".jpg");
            ImageMeta imageMeta = checkPreview.getPreviewMeta(task.getVideoDataId());
            TdApi.InputThumbnail thumb = (imageMeta != null)
                    ? new TdApi.InputThumbnail(thumbFile, imageMeta.width(), imageMeta.height())
                    : new TdApi.InputThumbnail(thumbFile, 1280, 720);

            TdApi.InputFileLocal inputFileLocal = new TdApi.InputFileLocal(pathVideo);
            TdApi.FormattedText formattedText = new TdApi.FormattedText(task.getVideoDataId().toString(), null);
            TdApi.InputMessageVideo inputMessageVideo = new TdApi.InputMessageVideo(
                    inputFileLocal, thumb,
                    null, task.getDuration(), 1920, 1080,
                    true, formattedText, null, false
            );

            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = chatId;
            sendMessage.inputMessageContent = inputMessageVideo;

            CompletableFuture<TdApi.File> completableFuture = new CompletableFuture<>();
            log.info("Начинаем отправку... {}", task.getVideoId());
            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Начинаем отправку...", InfoVideoType.INFO));

            client.send(sendMessage, result -> {
                if (result.isError()) {
                    completableFuture.completeExceptionally(new RuntimeException(result.getError().message));
                    return;
                }
                if (result.getConstructor() == TdApi.Message.CONSTRUCTOR) {
                    TdApi.Message message = (TdApi.Message) result;
                    TdApi.MessageContent content = message.content;
                    if (content.getConstructor() == TdApi.MessageVideo.CONSTRUCTOR) {
                        TdApi.MessageVideo mv = (TdApi.MessageVideo) content;
                        TdApi.File file = mv.video.video;
                        completableFuture.complete(file);
                    } else {
                        completableFuture.completeExceptionally(new RuntimeException("Unexpected message content"));
                    }
                } else {
                    completableFuture.completeExceptionally(new RuntimeException("Unexpected response"));
                }
            });

            TdApi.File uploaded = completableFuture.get(30, TimeUnit.MINUTES);
            videoDownloaded.saveVideoId(task.getVideoDataId(), uploaded.id);

            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Видео отправлено", InfoVideoType.INFO));
            log.info("Отправка завершена. videoDataId={} fileId={}", task.getVideoDataId(), uploaded.id);
            return true;

        } catch (TimeoutException e) {
            log.error("Таймаут при отправке видео. id:{}", task.getVideoDataId(), e);
            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Таймаут при отправке видео", InfoVideoType.ERROR));
            return false;
        } catch (Exception e) {
            log.error("Ошибка при отправке видео. id:{}", task.getVideoDataId(), e);
            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Ошибка при отправке видео", InfoVideoType.ERROR));
            return false;
        }
    }
}
