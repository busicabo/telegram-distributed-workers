package com.userbot.userbot.model;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
//Данные задачи для ее выполнения
public class Task {
    private Long videoDataId;
    private String url;
    private String videoId;
    private String audioId;
    private int duration;
}

