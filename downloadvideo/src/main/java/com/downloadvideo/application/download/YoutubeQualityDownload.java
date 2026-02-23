// YoutubeQualityDownload.java
package com.downloadvideo.application.download;

import com.downloadvideo.domain.model.ResponseDto;
import com.downloadvideo.domain.model.YoutubeQuality;
import com.downloadvideo.domain.model.YtDlpInfo;
import com.downloadvideo.domain.model.YtFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

/*
Класс для получения айди доступных качеств видео с выбора наилучшего видео для каждого качества
по разным критериям и с лимитом на размер не более 1700 мб (Telegram <= 2GB).
Так же выбирает доступные озвучки: Оригинальная, Русская и Английская.
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

            List<YoutubeQuality> qualities = pickBestMp4VideosPerHeight(info.getFormats(), url, audioPick);
            if (qualities.isEmpty()) return null;

            return new ResponseDto(info.getTitle(), qualities, info.getDuration());
        } catch (Exception e) {
            log.error("Ошибка парсинга/селекции форматов", e);
            return null;
        }
    }

    // 2) ЗАПУСК yt-dlp
    private String runYtDlpJson(String url) {
        List<String> cmd = new ArrayList<>(List.of(
                "yt-dlp",
                "--cookies-from-browser", "firefox:/config",
                "--js-runtimes", "node",
                "--no-warnings",
                "-q",
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
                out.append(new String(buf, 0, n, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            st.destroyForcibly();
            log.error("Ошибка при чтении вывода yt-dlp!", e);
            return null;
        }

        try {
            boolean finished = st.waitFor(1, TimeUnit.MINUTES);
            if (!finished) {
                st.destroyForcibly();
                log.error("yt-dlp не завершился за таймаут.");
                return null;
            }
            if (st.exitValue() != 0) {
                log.error("yt-dlp завершился с ошибкой. code={} output={}", st.exitValue(), out.toString());
                return null;
            }
            return out.toString();
        } catch (InterruptedException e) {
            st.destroyForcibly();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static class AudioPick {
        final String bestAudioFormatId;              // audio-only id (лучшее)
        final Map<String, String> langToFormatId;    // ru/en/uk если есть
        final boolean audioAvailable;                // есть ли вообще audio-only дорожки

        AudioPick(String bestAudioFormatId, Map<String, String> langToFormatId, boolean audioAvailable) {
            this.bestAudioFormatId = bestAudioFormatId;
            this.langToFormatId = langToFormatId;
            this.audioAvailable = audioAvailable;
        }
    }

    // 3) АУДИО (ИСПРАВЛЕНО: НИКОГДА НЕ ВОЗВРАЩАЕМ "MUXED" КАК АУДИО!)
    private AudioPick pickBestAudio(List<YtFormat> formats) {

        List<YtFormat> audioOnly = formats.stream()
                .filter(f -> "none".equalsIgnoreCase(f.getVcodec()))
                .filter(f -> f.getAcodec() != null && !"none".equalsIgnoreCase(f.getAcodec()))
                .collect(Collectors.toList());

        if (audioOnly.isEmpty()) {
            return new AudioPick(null, Map.of(), false);
        }

        // выбираем лучшее по abr
        YtFormat best = audioOnly.stream()
                .max(Comparator.comparing(f ->
                        f.getAbr() != null ? f.getAbr() : 0))
                .orElse(null);

        if (best == null) {
            return new AudioPick(null, Map.of(), false);
        }

        Map<String, String> langs = new HashMap<>();
        for (YtFormat f : audioOnly) {
            String lang = detectLang(f);
            if (!lang.isEmpty()) {
                langs.put(lang, f.getFormatId());
            }
        }

        return new AudioPick(best.getFormatId(), langs, true);
    }

    /**
     * Возвращает: "ru" / "en" / "uk" / "".
     */
    private String detectLang(YtFormat f) {
        String lang = f.getLanguage();
        if(lang==null) return "";
        if (!lang.isEmpty()) {
            if ("ua".equals(lang)) return "uk";
            if ("ru".equals(lang) || "en".equals(lang) || "uk".equals(lang)) return lang;
            if (lang.startsWith("ru")) return "ru";
            if (lang.startsWith("en")) return "en";
            if (lang.startsWith("uk")) return "uk";
        }

        if (containsAny(lang, " russian", "рус", " ru ", "ru-", "ru_", "[ru", "(ru", " дубляж", " озвуч")) return "ru";
        if (containsAny(lang, " english", "англ", " en ", "en-", "en_", "[en", "(en")) return "en";
        if (containsAny(lang, " ukrainian", "укра", " укр", " uk ", "uk-", "uk_", " ua ", "ua-", "ua_")) return "uk";

        return "";
    }

    private boolean containsAny(String hay, String... needles) {
        for (String n : needles) {
            if (hay.contains(n)) return true;
        }
        return false;
    }

    // 4) ВИДЕО (ИСПРАВЛЕНО: video-only mp4 + опционально muxed fallback)
    private List<YoutubeQuality> pickBestMp4VideosPerHeight(List<YtFormat> formats, String url, AudioPick audioPick) {

        BiFunction<String, String, String> nvl = (s, def) -> (s == null) ? def : s;

        Function<String, Integer> parseQualityFromFormatNote = (formatNote) -> {
            if (formatNote == null) return null;
            String s = formatNote.trim().toLowerCase(Locale.ROOT);
            int p = s.indexOf('p');
            if (p <= 0) return null;
            String left = s.substring(0, p).trim();
            if (left.isEmpty()) return null;
            for (int i = 0; i < left.length(); i++) if (!Character.isDigit(left.charAt(i))) return null;
            try { return Integer.parseInt(left); } catch (NumberFormatException e) { return null; }
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

        Set<Integer> allowedHeights = Set.of(144, 240, 360, 480, 720, 1080);

        // 1) основной пул: video-only mp4 (ВАЖНО!)
        List<YtFormat> videoOnlyMp4 = formats.stream()
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getVcodec(), "none")))
                .filter(f -> "mp4".equalsIgnoreCase(nvl.apply(f.getExt(), "")))
                .filter(f -> "none".equalsIgnoreCase(nvl.apply(f.getAcodec(), "none"))) // <-- FIX: только video-only
                .filter(f -> f.getFormatId() != null)
                .filter(f -> {
                    Long fs = f.getFilesize();
                    return fs == null || (fs / 1024 / 1024) < 1700;
                })
                .filter(f -> parseQualityFromFormatNote.apply(f.getFormatNote()) != null)
                .collect(Collectors.toList());

        // 2) fallback пул: muxed mp4 (если нет audio-only вообще или нет video-only для конкретного качества)
        List<YtFormat> muxedMp4 = formats.stream()
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getVcodec(), "none")))
                .filter(f -> "mp4".equalsIgnoreCase(nvl.apply(f.getExt(), "")))
                .filter(f -> !"none".equalsIgnoreCase(nvl.apply(f.getAcodec(), "none"))) // muxed
                .filter(f -> f.getFormatId() != null)
                .filter(f -> {
                    Long fs = f.getFilesize();
                    return fs == null || (fs / 1024 / 1024) < 1700;
                })
                .filter(f -> parseQualityFromFormatNote.apply(f.getFormatNote()) != null)
                .collect(Collectors.toList());

        Map<Integer, YtFormat> bestByHeight = new HashMap<>();

        // сначала выбираем video-only
        for (YtFormat f : videoOnlyMp4) {
            Integer h = parseQualityFromFormatNote.apply(f.getFormatNote());
            if (h == null || !allowedHeights.contains(h)) continue;

            YtFormat prev = bestByHeight.get(h);
            if (prev == null || videoBetterThan.test(f, prev)) {
                bestByHeight.put(h, f);
            }
        }

        // затем fallback: если для высоты ничего нет — берем muxed
        for (YtFormat f : muxedMp4) {
            Integer h = parseQualityFromFormatNote.apply(f.getFormatNote());
            if (h == null || !allowedHeights.contains(h)) continue;

            if (!bestByHeight.containsKey(h)) {
                bestByHeight.put(h, f);
            }
        }

        // собираем ответ:
        // - если выбранный видеоформат muxed => audioId = null (скачиваем одним форматом)
        // - если видео video-only, но аудио недоступно => тоже audioId = null
        return bestByHeight.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    YtFormat v = e.getValue();

                    boolean videoIsMuxed = !"none".equalsIgnoreCase(nvl.apply(v.getAcodec(), "none"));
                    String audioIdToUse = (videoIsMuxed || !audioPick.audioAvailable) ? null : audioPick.bestAudioFormatId;

                    int sizeMb = bytesToMb.applyAsInt(chooseSizeBytes.applyAsLong(v));
                    return new YoutubeQuality(
                            url,
                            v.getFormatId(),
                            audioIdToUse,
                            v.getFormatNote(),
                            sizeMb,
                            audioPick.langToFormatId
                    );
                })
                .collect(Collectors.toList());
    }
}
