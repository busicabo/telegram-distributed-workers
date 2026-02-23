package com.downloadvideo.application.telegram.handler;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import com.downloadvideo.domain.model.DownloadProcess;
import com.downloadvideo.domain.model.NewTaskStatus;
import com.downloadvideo.domain.model.Task;
import com.downloadvideo.adapters.in.telegram.dto.CallbackEvent;
import com.downloadvideo.adapters.out.persistence.entity.VideoDataEntity;
import com.downloadvideo.application.service.SendNewTask;
import com.downloadvideo.application.service.SendTelegram;
import com.downloadvideo.application.service.VideoDataService;
import com.downloadvideo.application.telegram.handler.main.CallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/*
Когда получили все данные для загрузки видео, отправляем задачу в kafka,
что бы распределитель задач забрал ее.
 */
@Slf4j
@Service
public class DownloadHandler implements CallbackHandler {
    @Autowired
    public VideoDataService videoDataService;
    @Autowired
    private SendNewTask sendNewTask;
    @Autowired
    private SendTelegram sendTelegram;


    @Override
    public boolean isValid(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith("dv:");
    }

    @Override
    public void handle(CallbackEvent event) {
        Update update = event.getUpdate();
        CallbackQuery callback = update.getCallbackQuery();
        String[] data = callback.getData().split(":");
        String videoDataId="";
        String audioId="";
        try{
            videoDataId=data[1];
            audioId=data[2];
        } catch (IndexOutOfBoundsException e){
            log.error("Не правильный формат данных в Callback data(озвучка)!",e);
            return;
        }

        VideoDataEntity videoData = videoDataService.getVideoData(Long.parseLong(videoDataId));
        if(videoData==null){
            EditMessageText edit = new EditMessageText();
            edit.setChatId(callback.getMessage().getChatId());
            edit.setMessageId(callback.getMessage().getMessageId());
            edit.setText("❗ Не удалось обработать данный видеоролик! Пожалуйста попробуйте еще раз!");
            sendTelegram.editMessage(edit);
            return;
        }
        videoData.setAudio_id(audioId);
        videoDataService.saveVideoData(videoData);
        if(!videoData.getUser_id().equals(callback.getFrom().getId())){
            return;
        }

        NewTaskStatus result = NewTaskStatus.FAIL;
        try{
            result = sendNewTask.start(new Task(videoData.getId(),videoData.getUrl(),videoData.getVideo_id(),audioId,videoData.getDuration()));
        } catch (Exception e){
            log.error("❗ Произошла ошибка при попытке добавить новую задачу в kafka!",e);
        }
        EditMessageText edit1 = new EditMessageText();
        edit1.setChatId(callback.getMessage().getChatId());
        edit1.setMessageId(callback.getMessage().getMessageId());
        if(result==NewTaskStatus.OK){
            edit1.setText("⏳ Обрабатываем...");
            sendTelegram.editMessage(edit1);
            videoDataService.completionProcess(videoData.getId(),DownloadProcess.DOWNLOAD);

        } else if(result==NewTaskStatus.FAIL){
            setStatusFailAndAndStop(videoData.getId());
            edit1.setText("❗ Не удалось отправить ваше видео на скачивание!");
            sendTelegram.editMessage(edit1);
        }
    }
    public void setStatusFailAndAndStop(Long videoDataId){
        videoDataService.completionProcess(videoDataId, DownloadProcess.FAIL);
    }

}
