package com.downloadvideo.service;

import com.downloadvideo.config.bot.MainBot;
import com.downloadvideo.model.DownloadProcess;
import com.downloadvideo.model.InfoVideo;
import com.downloadvideo.model.InfoVideoType;
import com.downloadvideo.model.postgresql.VideoDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

//Получает все статусы посланные ботами, для информирования пользователя.
@Service
@Slf4j
public class ListenStatus {
    @Autowired
    private VideoDataService videoDataService;
    @Autowired
    private MainBot mainBot;
    @Autowired
    private SendTelegram sendTelegram;
    @KafkaListener(topics = "${spring.kafka.consumer.topic-info-video}",groupId = "${spring.kafka.consumer.group-info-video}", containerFactory = "kafkaListenerContainerFactoryVideoInfo")
    public void listen(InfoVideo infoVideo, Acknowledgment acknowledgment){
        acknowledgment.acknowledge();
        VideoDataEntity videoData = videoDataService.getVideoData(infoVideo.getVideoDataId());
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(Math.toIntExact(videoData.getMessage_id()));
        editMessageText.setChatId(videoData.getChat_id());
        editMessageText.setText(infoVideo.getInfo());
        if(infoVideo.getType()== InfoVideoType.ERROR){
            editMessageText.setText("❗ "+editMessageText.getText());
            videoDataService.completionProcess(infoVideo.getVideoDataId(), DownloadProcess.FAIL);
        }
        sendTelegram.editMessage(editMessageText);

    }
}
