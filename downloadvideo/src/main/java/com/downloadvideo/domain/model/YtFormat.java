package com.downloadvideo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/*
Класс для парсинга json ответа от yt-dlp и дальнейшего наилучшего выбора
видео для каждого качества. Используется только в YoutubeQualityDownload
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class YtFormat {
    @JsonProperty("format_id")
    private String formatId;
    private String ext;
    private String vcodec;
    private String acodec;
    private String language;
    @JsonProperty("format_note")
    private String formatNote;
    private Double abr;
    private Double tbr;
    @JsonProperty("filesize")
    private Long filesize;
    @JsonProperty("filesize_approx")
    private Long filesizeApprox;
    @JsonProperty("source_preference")
    private Integer source_preference;

}
