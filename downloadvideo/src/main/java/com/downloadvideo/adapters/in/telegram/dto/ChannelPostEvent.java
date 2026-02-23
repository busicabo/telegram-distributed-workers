package com.downloadvideo.adapters.in.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
// Используется для публикации события о новом видео(его загрузки) в ApplicationEventPublisher
public class ChannelPostEvent {
    private Long videoDataId;
    private String fileId;
    private boolean isNew;
}
