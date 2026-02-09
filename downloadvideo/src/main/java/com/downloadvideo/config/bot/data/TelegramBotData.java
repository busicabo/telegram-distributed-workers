package com.downloadvideo.config.bot.data;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

//Данные нужные для Telegram Web Hook
@Configuration
@Data
public class TelegramBotData {
    @Value("${bot.data.name}")
    private String name;
    @Value("${bot.data.token}")
    private String token;
    @Value("${bot.data.url}")
    private String url;
    @Value("${bot.data.path}")
    private String path;


}
