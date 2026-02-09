package com.downloadvideo.service.downloads;

import com.downloadvideo.model.ResponseDto;
import com.downloadvideo.model.YoutubeQuality;
import com.downloadvideo.model.YtDlpInfo;
import com.downloadvideo.model.YtFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

/*
Класс для получения айди доступных качеств видео с выбора наилучшего видео для каждого качества
по разным критериям и с лимитом на размер не более 1700 мб(Телеграм позволяет отправить не более 2гб.
Это предосторожность).
Так же выбирает доступные озвучки: Оригинальная, Русская и Английская
*/
@Slf4j
@Service
public class YoutubeQualityDownload {
    @Value("${cookies.browserPath}")
    private String browserPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1) ГЛАВНЫЙ МЕТОД
    public ResponseDto giveQualityDownload(String url) {
        String json = runYtDlpJson(url);
        if (json == null || json.isBlank()) return null;

        try {
            YtDlpInfo info = objectMapper.readValue(json, YtDlpInfo.class);
            if (info.getFormats() == null || info.getFormats().isEmpty()) {
                log.warn("formats пустой в ответе yt-dlp");
                return null;
            }

            AudioPick audioPick = pickBestAudio(info.getFormats());
            if (audioPick == null) {
                log.warn("Аудио дорожки не найдены");
                return null;
            }

            List<YoutubeQuality> qualities = pickBestMp4VideosPerHeight(info.getFormats(), url, audioPick);
            if (qualities.isEmpty()) return null;

            return new ResponseDto(info.getTitle(), qualities,info.getDuration());
        } catch (Exception e) {
            log.error("Ошибка парсинга/селекции форматов", e);
            return null;
        }
    }

    // 2) ЗАПУСК yt-dlp
    private String runYtDlpJson(String url) {
        File file = new File(browserPath);
        List<String> cmd = new ArrayList<>(List.of(
                "yt-dlp",
                "--ignore-config",
                "--cookies-from-browser", "firefox:"+browserPath,
                "--js-runtimes", "node",
                "-q",
                "--no-warnings",
                "-J",
                url
        ));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process st;
        try {
            st = pb.start();
        } catch (IOException e) {
            log.error("Не удалось запустить yt-dlp!", e);
            return null;
        }

        StringBuilder out = new StringBuilder();
        try (InputStream is = st.getInputStream()) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.append(new String(buf, 0, n));
            }
        } catch (IOException e) {
            st.destroyForcibly();
            log.error("Ошибка при чтении вывода yt-dlp!", e);
            return null;
        }

        try {
            boolean finished = st.waitFor(1, TimeUnit.MINUTES); // пример
            if (!finished) {
                st.destroyForcibly();
                log.error("yt-dlp не завершился за таймаут.");
                return null;
            }
            if (st.exitValue() != 0) {
                log.error("yt-dlp завершился с ошибкой. code={} ошибка={}", st.exitValue(), out.toString());
                return null;
            }
            return out.toString();
        } catch (InterruptedException e) {
            st.destroyForcibly();
            return null;
        }
    }

    private static class AudioPick {
        final String bestAudioFormatId;
        final Map<String, String> langToFormatId; // ru/en если есть

        AudioPick(String bestAudioFormatId, Map<String, String> langToFormatId) {
            this.bestAudioFormatId = bestAudioFormatId;
            this.langToFormatId = langToFormatId;
        }
    }

    // 3) АУДИО
    private AudioPick pickBestAudio(List<YtFormat> formats) {

        BiFunction<String, String, String> nvl =
                (s, def) -> (s == null) ? def : s;

        List<YtFormat> audioOnly = formats.stream()
                .filter(f -> "none".equalsIgnoreCase(nvl.apply(f.getVcodec(), "none")))
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getAcodec(), "none")))
                .filter(f -> f.getFormatId() != null)
                .collect(Collectors.toList());

        Map<String, String> langs = new HashMap<>();
        for (YtFormat f : audioOnly) {
            String lang = nvl.apply(f.getLanguage(), "").toLowerCase(Locale.ROOT);
            if ("ru".equals(lang)) langs.put("ru", f.getFormatId());
            if ("en".equals(lang)) langs.put("en", f.getFormatId());
        }

        Comparator<YtFormat> byAudioScore = Comparator.comparingDouble(f -> {
            if (f.getAbr() != null && f.getAbr() > 0) return f.getAbr();
            if (f.getTbr() != null && f.getTbr() > 0) return f.getTbr();
            return -1.0;
        });

        YtFormat best = audioOnly.stream().max(byAudioScore).orElse(null);
        if (best != null) return new AudioPick(best.getFormatId(), langs);

        YtFormat muxed = formats.stream()
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getVcodec(), "none")))
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getAcodec(), "none")))
                .filter(f -> f.getFormatId() != null)
                .findFirst()
                .orElse(null);

        if (muxed == null) return null;
        return new AudioPick(muxed.getFormatId(), langs);
    }

    // 4) ВИДЕО
    private List<YoutubeQuality> pickBestMp4VideosPerHeight(List<YtFormat> formats, String url, AudioPick audioPick) {

        BiFunction<String, String, String> nvl =
                (s, def) -> (s == null) ? def : s;

        Function<String, Integer> parseQualityFromFormatNote = (formatNote) -> {
            if (formatNote == null) return null;
            String s = formatNote.trim().toLowerCase(Locale.ROOT);
            int p = s.indexOf('p');
            if (p <= 0) return null;

            String left = s.substring(0, p).trim();
            if (left.isEmpty()) return null;

            for (int i = 0; i < left.length(); i++) {
                if (!Character.isDigit(left.charAt(i))) return null;
            }

            try {
                return Integer.parseInt(left);
            } catch (NumberFormatException e) {
                return null;
            }
        };

        ToLongFunction<YtFormat> chooseSizeBytes = (f) -> {
            if (f.getFilesizeApprox() != null && f.getFilesizeApprox() > 0) return f.getFilesizeApprox();
            if (f.getFilesize() != null && f.getFilesize() > 0) return f.getFilesize();
            return -1;
        };

        LongToIntFunction bytesToMb = (bytes) -> {
            if (bytes <= 0) return 0;
            return (int) (bytes / 1024 / 1024);
        };

        BiPredicate<YtFormat, YtFormat> videoBetterThan = (a, b) -> {
            long sa = chooseSizeBytes.applyAsLong(a);
            long sb = chooseSizeBytes.applyAsLong(b);

            if (sa > 0 && sb > 0) return sa > sb;

            double ta = (a.getTbr() != null) ? a.getTbr() : -1;
            double tb = (b.getTbr() != null) ? b.getTbr() : -1;
            return ta > tb;
        };

        List<YtFormat> videos = formats.stream()
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getVcodec(), "none")))
                .filter(f -> "mp4".equalsIgnoreCase(nvl.apply(f.getExt(), "")))
                .filter(f -> f.getFormatId() != null)
                .filter(f -> {
                    Long fs = f.getFilesize();
                    return fs == null || (fs / 1024 / 1024) < 1700;
                })
                .filter(f -> parseQualityFromFormatNote.apply(f.getFormatNote()) != null)
                .collect(Collectors.toList());

        Set<Integer> allowedHeights = Set.of(144, 240, 360, 480, 720, 1080);

        Map<Integer, YtFormat> bestByHeight = new HashMap<>();
        for (YtFormat f : videos) {
            Integer h = parseQualityFromFormatNote.apply(f.getFormatNote());
            if (h == null) continue;
            if (!allowedHeights.contains(h)) continue;

            YtFormat prev = bestByHeight.get(h);
            if (prev == null || videoBetterThan.test(f, prev)) {
                bestByHeight.put(h, f);
            }
        }

        return bestByHeight.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    YtFormat f = e.getValue();
                    int sizeMb = bytesToMb.applyAsInt(chooseSizeBytes.applyAsLong(f));
                    return new YoutubeQuality(
                            url,
                            f.getFormatId(),
                            audioPick.bestAudioFormatId,
                            f.getFormatNote(),
                            sizeMb,
                            audioPick.langToFormatId
                    );
                })
                .collect(Collectors.toList());
    }
}
