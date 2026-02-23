package com.userbot.userbot.domain.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
//Класс для передачи статуса задания в главного Telegram Bot Api
public class InfoVideo {
    private Long videoDataId;
    private String info;
    private InfoVideoType type;
}
