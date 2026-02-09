package com.downloadvideo.service.handler;

import com.downloadvideo.model.event.ChannelPostEvent;
import com.downloadvideo.model.postgresql.DownloadedVideosEntity;
import com.downloadvideo.model.postgresql.VideoDataEntity;
import com.downloadvideo.service.DownloadedVideosService;
import com.downloadvideo.service.SendTelegram;
import com.downloadvideo.service.SendVideoToUser;
import com.downloadvideo.service.VideoDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/*
Когда бот отправил скачаное видео в телеграм группу, этот класс его принимает
 и решает, сохранить ли его в бд для моментально отправки(если видео новое),
 после чего отправляет в следующий сервис который уже отправит видео пользователю
 и завершит задачу.
 */
@Service
@Slf4j
public class HandlerSendVideoToUser {
    @Autowired
    private SendVideoToUser sendVideoToUser;
    @Autowired
    private VideoDataService videoDataService;
    @Autowired
    private DownloadedVideosService downloadedVideosService;
    public void sendVideo(ChannelPostEvent event){
        Long taskId = event.getVideoDataId();
        String fileId = event.getFileId();
        boolean isNew = event.isNew();
        VideoDataEntity videoData = videoDataService.getVideoData(taskId);
        if(videoData==null){
            log.info("Айди задачи не найден(HandlerSendVideoToUser)! id:{}",taskId);
            return;
        }
        if(isNew){
            DownloadedVideosEntity downloadedVideosEntity = new DownloadedVideosEntity(
                    videoData.getUrl(),videoData.getVideo_id(),videoData.getAudio_id(),fileId,videoData.getId());
            DownloadedVideosEntity result = null;
            try{
                result = downloadedVideosService.save(downloadedVideosEntity);
                log.info("Видео успешно сохранено! id:{}",result.getVideoDataId());
            } catch (Exception e){
                log.error("Не сохранили видео в бд: ",e);
            }
        }
        sendVideoToUser.sendVideo(videoData,fileId);
    }

    @EventListener
    public void listen(ChannelPostEvent event){
        sendVideo(event);
    }

}
