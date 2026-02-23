package com.downloadvideo.adapters.in.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoEvent {
    private Message message;
}
