package com.userbot.userbot.service;

import com.userbot.userbot.model.DownloadPath;
import com.userbot.userbot.model.InfoVideo;
import com.userbot.userbot.model.InfoVideoType;
import com.userbot.userbot.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
//Скаичвает видео на устройство
public class DownloadVideoToYourDevice {
    private String path = DownloadPath.PATH;
    @Value("${bot.cookies.cookies-path}")
    private String cookiesPath;
    @Autowired
    private SendStatus sendStatus;

    public boolean download(Task task){
        Long videoDataId = task.getVideoDataId();
        String url = task.getUrl();
        String video_id=task.getVideoId();
        String audio_id=task.getAudioId();
        System.out.println(new File("/config/profile").exists());
        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "--cookies-from-browser", "firefox:/config/profile",
                "--js-runtimes", "node",
                "-f", task.getVideoId() + "+" + task.getAudioId(),
                "--merge-output-format", "mp4",
                "-o", Paths.get(path, videoDataId + ".%(ext)s").toString(),
                url
        );
        pb.redirectErrorStream(true);

        Process st;
        try {
            sendStatus.sendStatus(new InfoVideo(videoDataId, "Начинаем загрузку вашего видео...", InfoVideoType.INFO));
            st = pb.start();
        } catch (IOException e) {
            log.error("Не удалось запустить yt-dlp!", e);
            return false;
        }

        StringBuilder out = new StringBuilder();
        try (InputStream is = st.getInputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.append(new String(buf, 0, n));
            }
        } catch (IOException e) {
            st.destroyForcibly();
            log.error("Ошибка при чтении вывода yt-dlp!", e);
            return false;
        }

        try {
            boolean finished = st.waitFor(60, TimeUnit.MINUTES); // пример
            if (!finished) {
                st.destroyForcibly();
                log.error("yt-dlp не завершился за таймаут. task_id={}", videoDataId);
                return false;
            }
            if (st.exitValue() != 0) {
                log.error("yt-dlp завершился с ошибкой. code={} output={}", st.exitValue(), out.toString());
                return false;
            }
            log.info("Передача видео для отправки! url: {} task_id: {}", url, videoDataId);
            return true;
        } catch (InterruptedException e) {
            st.destroyForcibly();
            return false;
        }
    }
}
