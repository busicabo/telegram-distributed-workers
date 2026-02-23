package com.downloadvideo.infrastructure.aop;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.application.service.AdminPanel;
import com.downloadvideo.application.service.SendTelegram;
import com.downloadvideo.application.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Aspect
@Service
@Slf4j
//Отправка всех ошибок админам
public class CheckAndSendError {
    @Autowired
    private AdminPanel adminPanel;
    @Autowired
    private SendTelegram sendTelegram;
    @AfterThrowing(
            pointcut = "execution(* com.downloadvideo.application.service..*(..))",
            throwing = "ex"
    )
    public void onError(Exception ex) {
        if (!adminPanel.isSendError()) return;
        for(Long id: adminPanel.getAllAdmins()){
            sendTelegram.sendMessage(new SendMessage(String.valueOf(id),"Ошибка: "+ex.getMessage()));
        }
    }
}
