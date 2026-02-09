package com.taskdistributor.taskdistributor.model;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
//Задача
public class Task {
    private Long videoDataId;
    private String url;
    private String videoId;
    private String audioId;
    private int duration;
}
