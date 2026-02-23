package com.userbot.userbot.adapters.in.telegram.listener;

import it.tdlight.jni.TdApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener {
    private final ApplicationEventPublisher applicationEventPublisher;
    public ApplicationEventListener(ApplicationEventPublisher applicationEventPublisher){
        this.applicationEventPublisher=applicationEventPublisher;
    }
    public void publisherUpdate(Object update){
        applicationEventPublisher.publishEvent(update);
    }

}
