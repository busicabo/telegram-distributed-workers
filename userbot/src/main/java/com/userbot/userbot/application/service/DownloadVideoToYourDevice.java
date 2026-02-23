package com.userbot.userbot.application.service;

import com.userbot.userbot.application.port.out.StatusPublisherPort;
import com.userbot.userbot.domain.model.DownloadPath;
import com.userbot.userbot.domain.model.InfoVideo;
import com.userbot.userbot.domain.model.InfoVideoType;
import com.userbot.userbot.domain.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
// Скачивает видео на устройство (через yt-dlp)
public class DownloadVideoToYourDevice {

    private final StatusPublisherPort statusPublisher;

    private final String path = DownloadPath.PATH;

    public boolean download(Task task) {
        Long videoDataId = task.getVideoDataId();

        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "--cookies-from-browser", "firefox:/config/profile",
                "--js-runtimes", "node",
                "-f", task.getVideoId() + "+" + task.getAudioId(),
                "--merge-output-format", "mp4",
                "-o", Paths.get(path, videoDataId + ".%(ext)s").toString(),
                task.getUrl()
        );
        pb.redirectErrorStream(true);

        Process proc;
        try {
            statusPublisher.publish(new InfoVideo(videoDataId, "Начинаем загрузку вашего видео...", InfoVideoType.INFO));
            proc = pb.start();
        } catch (IOException e) {
            log.error("Не удалось запустить yt-dlp!", e);
            return false;
        }

        StringBuilder out = new StringBuilder();
        try (InputStream is = proc.getInputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.append(new String(buf, 0, n));
            }
        } catch (IOException e) {
            proc.destroyForcibly();
            log.error("Ошибка при чтении вывода yt-dlp!", e);
            return false;
        }

        try {
            boolean finished = proc.waitFor(60, TimeUnit.MINUTES);
            if (!finished) {
                proc.destroyForcibly();
                log.error("yt-dlp не завершился за таймаут. task_id={}", videoDataId);
                return false;
            }
            if (proc.exitValue() != 0) {
                log.error("yt-dlp завершился с ошибкой. code={} output={}", proc.exitValue(), out.toString());
                return false;
            }
            log.info("Видео скачано. task_id={}", videoDataId);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            return false;
        }
    }
}
