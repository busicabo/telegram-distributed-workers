package com.downloadvideo.model;

import lombok.*;

/*
Используется только для передачи информации на внешний
сервер, что бы бот смог скачать видео и присвоит нужному пользователю.
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private Long videoDataId;
    private String url;
    private String videoId;
    private String audioId;
    private int duration;
}
