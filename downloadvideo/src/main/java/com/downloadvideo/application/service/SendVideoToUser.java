package com.downloadvideo.application.service;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.domain.model.DownloadProcess;
import com.downloadvideo.adapters.out.persistence.entity.VideoDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;


//Отправляет видео пользователям
@Slf4j
@Service
public class SendVideoToUser {
    @Autowired
    private MainBot mainBot;
    @Autowired
    private UserService userService;
    @Autowired
    private VideoDataService videoDataService;
    @Autowired
    private SendTelegram sendTelegram;

    public void sendVideo(VideoDataEntity videoData,String fileId){
        DeleteMessage deleteMessage = new DeleteMessage(videoData.getChat_id().toString(),videoData.getMessage_id().intValue());
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(videoData.getChat_id());
        sendVideo.setVideo(new InputFile(fileId));
        sendVideo.setCaption(
                "🎬 " + videoData.getVideoName() + "\n\n" +
                        "🤖 @ytfa_bot"
        );
        try {
            sendTelegram.sendVideo(sendVideo);
            videoDataService.completionProcess(videoData.getId(),DownloadProcess.FINISH);
            log.info("Успешная отправка видео пользователю! id задачи:{}",videoData.getId());
            sendTelegram.deleteMessage(deleteMessage);
            userService.addCountDownloads(videoData.getUser_id());

        } catch (Exception e) {
            videoDataService.completionProcess(videoData.getId(),DownloadProcess.FAIL);
            log.error("Произошла ошибка при отправке видео пользователю!",e);
        }
    }

}
