package com.downloadvideo.aop;

import com.downloadvideo.model.NewTaskStatus;
import com.downloadvideo.model.Task;
import com.downloadvideo.model.event.ChannelPostEvent;
import com.downloadvideo.service.DownloadedVideosService;
import com.downloadvideo.service.handler.HandlerSendVideoToUser;
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
    @Around("execution(* com.downloadvideo.service.SendNewTask.start(com.downloadvideo.model.Task))")
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
