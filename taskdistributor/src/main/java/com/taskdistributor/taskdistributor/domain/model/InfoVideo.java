package com.taskdistributor.taskdistributor.domain.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
//Отправляет статус задачи в главного бота.
public class InfoVideo {
    private Long videoDataId;
    private String info;
    private InfoVideoType type;
}
