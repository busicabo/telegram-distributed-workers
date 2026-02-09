package com.downloadvideo.aop;

import com.downloadvideo.model.event.MessageEvent;
import com.downloadvideo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Aspect
@Service
//Проверка на права админа при взаимодействии с админ командами.
public class CheckAdminGroup {
    @Autowired
    private UserService userService;
    @Around("execution(* com.downloadvideo.service.handler.AdminCommandHandler.handle(com.downloadvideo.model.event.MessageEvent))")
    public Object check(ProceedingJoinPoint point){
        MessageEvent event = (MessageEvent) point.getArgs()[0];
        Long id = event.getUpdate().getMessage().getFrom().getId();
        if(!userService.isAdmin(id)){
            return null;
        }
        try {
            return point.proceed();
        } catch (Throwable e) {
            log.error("Ошибка при проверке права на админа!",e);
        }
        return null;
    }
}
