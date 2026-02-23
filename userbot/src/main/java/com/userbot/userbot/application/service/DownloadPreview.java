package com.userbot.userbot.application.service;

import com.userbot.userbot.domain.model.DownloadPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
//Скачивает превью
public class DownloadPreview{
    private String path = DownloadPath.PATH;
    public boolean downloadPreview(String url,Long id){
        List<String> cmd = List.of(
                "yt-dlp",
                "--skip-download",
                "--write-thumbnail",
                "--convert-thumbnails", "jpg",
                "-o", Paths.get(path, id + ".%(ext)s").toString(),
                url
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
            try {
                return p.waitFor(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Ошибка при ожидание скачивания превью!",e);
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            log.error("Произошла ошибка при скачивание превью через yt-dlp! ",e);
        }
        return false;
    }
}
