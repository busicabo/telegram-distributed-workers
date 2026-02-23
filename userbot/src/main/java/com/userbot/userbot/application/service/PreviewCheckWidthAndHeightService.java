package com.userbot.userbot.application.service;

import com.userbot.userbot.domain.model.DownloadPath;
import com.userbot.userbot.domain.model.ImageMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

//Высчитывает точный размер превью, что бы телеграм не показывал черный экран
@Service
public class PreviewCheckWidthAndHeightService {
    private String path = DownloadPath.PATH;
    public ImageMeta getPreviewMeta(Long id) throws Exception {
        Path dir = Path.of(path);

        // на случай, если yt-dlp оставит другое имя/расширение - найдём по префиксу id
        Optional<Path> fileOpt;
        try (Stream<Path> s = Files.list(dir)) {
            fileOpt = s.filter(p -> p.getFileName().toString().startsWith(id.toString()))
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jpg"))
                    .findFirst();
        }

        Path jpg = fileOpt.orElse(null);

        if(jpg==null){
            return null;
        }
        long bytes = Files.size(jpg);

        BufferedImage img = ImageIO.read(jpg.toFile());
        if (img == null) {
            return null;
        }

        return new ImageMeta(img.getWidth(), img.getHeight());
    }
}
