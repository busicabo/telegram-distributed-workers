package com.downloadvideo.infrastructure.aop;

import com.downloadvideo.domain.model.NewTaskStatus;
import com.downloadvideo.domain.model.Task;
import com.downloadvideo.adapters.in.telegram.dto.ChannelPostEvent;
import com.downloadvideo.application.service.DownloadedVideosService;
import com.downloadvideo.application.telegram.handler.HandlerSendVideoToUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Aspect
@Service
@Slf4j
//Проверяет скачивали это видео раньше. Если да, то сразу отправляет видео по его хэшу.
public class CheckExistingVideo {
    @Autowired
    private DownloadedVideosService downloadedVideosService;
    @Autowired
    private HandlerSendVideoToUser handlerSendVideoToUser;
    @Around("execution(* com.downloadvideo.application.service.SendNewTask.start(com.downloadvideo.domain.model.Task))")
    public Object start(ProceedingJoinPoint jp){
        Task task = (Task)jp.getArgs()[0];
        String fileId = downloadedVideosService.getFileId(task);
        if(fileId==null || fileId.isEmpty()){
            try {
                return (NewTaskStatus) jp.proceed();
            } catch (Throwable e) {
                log.error("Ошибка при отправке задачи в kafka(aop)!",e);
                return false;
            }
        }
        log.info("Видео было найдено в бд! fileId:{}",fileId );
        handlerSendVideoToUser.sendVideo(new ChannelPostEvent(task.getVideoDataId(),fileId,false));
        return NewTaskStatus.EXISTS;
    }
}
