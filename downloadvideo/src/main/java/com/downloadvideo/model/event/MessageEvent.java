package com.downloadvideo.model.event;

import com.downloadvideo.config.bot.MainBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
// Используется для публикации события о новом сообщении от пользователя в ApplicationEventPublisher
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MessageEvent {
    private Update update;
}
