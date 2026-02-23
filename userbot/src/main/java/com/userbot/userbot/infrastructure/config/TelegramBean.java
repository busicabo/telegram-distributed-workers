package com.userbot.userbot.infrastructure.config;

import com.userbot.userbot.adapters.in.telegram.listener.ApplicationEventListener;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class TelegramBean {
    @Autowired
    private ApplicationEventListener applicationEventListener;

    @Value("${tdlib.id}")
    private int id;
    @Value("${tdlib.hash}")
    private String hash;
    @Value("${tdlib.pathDataBase}")
    private String pathDataBase;
    @Value("${tdlib.pathDownload}")
    private String pathDownload;
    @Value("${tdlib.phoneNumber}")
    private String phoneNumber;

    @Bean
    public SimpleTelegramClient simpleTelegramClient(){
        SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();
        APIToken apiToken = new APIToken(id,hash);
        TDLibSettings settings = TDLibSettings.create(apiToken);
        settings.setDatabaseDirectoryPath(Path.of(pathDataBase));
        settings.setDownloadedFilesDirectoryPath(Path.of(pathDownload));
        SimpleTelegramClientBuilder builder = clientFactory.builder(settings);
        builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class,auth ->{
            applicationEventListener.publisherUpdate(auth.authorizationState);
        });
        builder.addUpdateHandler(TdApi.UpdateNewMessage.class, update ->{
            applicationEventListener.publisherUpdate(update);
        });
        builder.addUpdateHandler(TdApi.UpdateMessageSendSucceeded.class, update->{
            applicationEventListener.publisherUpdate(update);
        });
        builder.addUpdateHandler(TdApi.UpdateMessageSendFailed.class, update ->{
            applicationEventListener.publisherUpdate(update);
        });
        return builder.build(AuthenticationSupplier.user(phoneNumber));
    }
}
