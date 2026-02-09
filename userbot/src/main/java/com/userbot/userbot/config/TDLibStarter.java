package com.userbot.userbot.config;

import it.tdlight.Init;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.util.UnsupportedNativeLibraryException;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TDLibStarter {

    @PostConstruct
    public void startInit(){
        try {
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            throw new RuntimeException(e);
        }
    }
}
