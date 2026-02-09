package com.downloadvideo.model;

import lombok.*;

import java.util.Map;
//Итоговый объект для наилучшего видео для каждого качества с полной информации о видео
@AllArgsConstructor
@Setter
@Getter
@ToString
@NoArgsConstructor
public class YoutubeQuality {
    private String url;
    private String videoId;
    private String musicId;
    private String format_note;
    private int size;
    private Map<String,String> translateVoice;
    private int width;
    private String formatNote;
    private int height;
    public YoutubeQuality(String url, String videoId, String musicId, String format_note, int size,Map<String,String> translateVoice) {
        this.formatNote = format_note;
        this.url = url;
        this.videoId = videoId;
        this.musicId = musicId;
        this.format_note = format_note;
        this.size = size;
        this.translateVoice=translateVoice;

    }
}
