package com.downloadvideo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/*
Класс для парсинга json ответа от yt-dlp для получения всех доступных качеств
видео для каждого качества. Используется только в YoutubeQualityDownload
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtDlpInfo {
    private String title;
    private List<YtFormat> formats;
    private Integer duration;
}