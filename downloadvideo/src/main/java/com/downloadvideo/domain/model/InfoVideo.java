package com.downloadvideo.domain.model;

import lombok.*;

// Получение статуса задачи от бота по скачиванию видео
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class InfoVideo {
    private Long videoDataId;
    private String info;
    private InfoVideoType type;
}
