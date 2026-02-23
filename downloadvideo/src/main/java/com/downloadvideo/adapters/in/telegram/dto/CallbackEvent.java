package com.downloadvideo.adapters.in.telegram.dto;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
// Используется для публикации события о новом нажатии на кнопку от пользователя в ApplicationEventPublisher
public class CallbackEvent {
    private Update update;
}
