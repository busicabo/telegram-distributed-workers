package com.downloadvideo.infrastructure.aop;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.application.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Aspect
@Service
@Slf4j
/*
Аутентификация апдейта на проверку прав доступа для дальнейшего продолжения работы.
(проще говоря заблокирован ли пользователь, существует ли он(если нет создать),
является ли данные канал источником загруженных ботов видео.
 */
public class CheckUser {
    @Value("${chat-id}")
    private String CHATS_ID;
    @Autowired
    private UserService userService;
    @Around("execution(void com.downloadvideo.application.telegram.handler.main.MainHandler.handler(org.telegram.telegrambots.meta.api.objects.Update))")
    public void check(ProceedingJoinPoint pjp) throws Throwable {
        Update update = (Update) pjp.getArgs()[0];
        long userId = 0L;
        String username = "";
        if(update.hasChannelPost()){
            for(String chatId: CHATS_ID.split(",")){
                if(String.valueOf(update.getChannelPost().getChatId()).equals(chatId)){
                    pjp.proceed();
                }
            }
            return;
        }
        if(update.hasMessage()){
            userId=update.getMessage().getFrom().getId();
            username=update.getMessage().getFrom().getUserName();
        } else if(update.hasCallbackQuery()){
            userId=update.getCallbackQuery().getFrom().getId();
            username=update.getCallbackQuery().getFrom().getUserName();
        }
        if (userId <= 0) return;
        try {
            if(userService.authenticateUser(userId,username)){
                log.info("Аутентификация пройдена: id:{}, username:{}",userId,username);
                pjp.proceed();
                return;
            }
            log.info("Аутентификация не пройдена: id:{}, username:{}",userId,username);
            return;
        } catch (Throwable e) {
            log.error("Ошибка начала задачи(MainBot)!",e);
            return;
        }
    }
}
