package com.downloadvideo.domain.model;

import lombok.*;

import java.util.List;

//Итоговый объект, содержащий все доступные видео для скачивания и прочую важную информацию по этому видео
@AllArgsConstructor
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ResponseDto {
    private String videoName;
    private List<YoutubeQuality> youtubeQuality;
    private Integer duration;

}
