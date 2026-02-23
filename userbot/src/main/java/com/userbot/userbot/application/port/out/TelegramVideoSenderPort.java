package com.userbot.userbot.application.port.out;

import com.userbot.userbot.domain.model.Task;

public interface TelegramVideoSenderPort {
    boolean sendVideo(Task task);
}
