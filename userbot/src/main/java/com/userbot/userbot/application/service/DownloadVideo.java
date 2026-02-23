package com.userbot.userbot.application.service;

import com.userbot.userbot.application.port.out.StatusPublisherPort;
import com.userbot.userbot.application.port.out.TelegramVideoSenderPort;
import com.userbot.userbot.domain.model.DownloadPath;
import com.userbot.userbot.domain.model.InfoVideo;
import com.userbot.userbot.domain.model.InfoVideoType;
import com.userbot.userbot.domain.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
// Главный use-case по скачиванию и отправке видео. Управляет всеми нужными процессами для выполнения задачи.
public class DownloadVideo {

    private final TelegramVideoSenderPort telegramVideoSender;
    private final DownloadVideoToYourDevice downloadVideoToYourDevice;
    private final StatusPublisherPort statusPublisher;
    private final DownloadPreview downloadPreview;

    private final String path = DownloadPath.PATH;

    public boolean start(Task task) {
        try {
            boolean downloaded = downloadVideoToYourDevice.download(task);
            if (downloaded) {
                boolean previewOk = downloadPreview.downloadPreview(task.getUrl(), task.getVideoDataId());
                log.info(previewOk ? "Успешная загрузка превью! id:{}" : "Превью не было загружено! id:{}", task.getVideoDataId());

                boolean sent = telegramVideoSender.sendVideo(task);
                if (sent) {
                    log.info("Задача прошла успешно! id: {}", task.getVideoDataId());
                    return true;
                }
            }

            log.info("Задача не прошла успешно :( ! id:{}", task.getVideoDataId());
            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Возникла ошибка при скачивании/отправке видео", InfoVideoType.ERROR));
            return false;

        } catch (Exception e) {
            log.error("Неудачное скачивание/отправка :( ", e);
            statusPublisher.publish(new InfoVideo(task.getVideoDataId(), "Возникла ошибка при скачивании/отправке видео", InfoVideoType.ERROR));
            return false;

        } finally {
            cleanupTaskFiles(task);
        }
    }

    private void cleanupTaskFiles(Task task) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.getName().startsWith(task.getVideoDataId().toString())) {
                // best-effort
                if (!f.delete()) {
                    log.debug("Не удалось удалить файл: {}", f.getAbsolutePath());
                }
            }
        }
    }
}
