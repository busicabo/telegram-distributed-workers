package com.userbot.userbot.application.port.out;

import com.userbot.userbot.domain.model.InfoVideo;

public interface StatusPublisherPort {
    void publish(InfoVideo infoVideo);
}
