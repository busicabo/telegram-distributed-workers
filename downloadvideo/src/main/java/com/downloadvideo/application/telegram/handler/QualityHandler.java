package com.downloadvideo.application.telegram.handler;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/*
После выбора качества видео, ищем озвучки(en,ru) и просим пользователя выбрать какую
озвучку он хочет. Если только оригинальная озвучка, то сразу отправляем задачу в распределитель задач.
 */
@Slf4j
@Service
public class QualityHandler implements CallbackHandler {
    @Autowired
    public VideoDataService videoDataService;
    @Autowired
    private SendNewTask sendNewTask;
    @Autowired
    private SendTelegram sendTelegram;
    @Override
    public boolean isValid(CallbackQuery callbackQuery) {
        return callbackQuery.getData().startsWith("qy:");
    }


    @Override
    public void handle(CallbackEvent event) {
        Update update = event.getUpdate();
        CallbackQuery callback = update.getCallbackQuery();
        String[] data = callback.getData().split(":");
        String videoDataId="";
        String userId="";
        String videoId="";
        String videoSize="";
        String videoFormatNote="";
        try{
            videoDataId=data[1];
            userId=data[2];
            videoId=data[3];
            videoSize=data[4];
            videoFormatNote=data[5];

        } catch (IndexOutOfBoundsException e){
            log.error("Неправильная форма данных в callback!",e);
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
        if(!videoData.getUser_id().equals(callback.getFrom().getId())){
            return;
        }
        videoData.setFormat_note(videoFormatNote);
        videoData.setMessage_id(Long.valueOf(callback.getMessage().getMessageId()));
        videoData.setSize(Long.parseLong(videoSize));
        videoData.setVideo_id(videoId);
        videoDataService.saveVideoData(videoData);
        String ru=videoData.getRu_id()!=null?videoData.getRu_id():"";
        String en=videoData.getEn_id()!=null?videoData.getEn_id():"";
        String uk=videoData.getUk_id()!=null?videoData.getUk_id():"";
        if(!videoData.getUser_id().equals(callback.getFrom().getId()) || !videoData.getUser_id().equals(Long.parseLong(userId))){
            return;
        }
        //Если есть только оригинальная озвучка, то пропускаем выбор озвучки и сразу отправляем готовое задание распределителю задач.
        if((ru.isEmpty() || ru.equals(videoData.getAudio_id()))
        && (en.isEmpty() || en.equals(videoData.getAudio_id()))
        && (uk.isEmpty() || uk.equals(videoData.getAudio_id()))){
            NewTaskStatus result = NewTaskStatus.FAIL;
            try{
                result = sendNewTask.start(new Task(videoData.getId(),videoData.getUrl(),videoData.getVideo_id(),videoData.getAudio_id(),videoData.getDuration()));
            } catch (Exception e){
                log.error("Произошла ошибка при попытке добавить новую задачу в kafka!",e);
            }
            EditMessageText edit1 = new EditMessageText();
            edit1.setChatId(callback.getMessage().getChatId());
            edit1.setMessageId(callback.getMessage().getMessageId());
            if(result==NewTaskStatus.OK){
                edit1.setText("Найдена только оригинальная озвучка.\n⏳ Обрабатываем...");
                sendTelegram.editMessage(edit1);
            } else if(result==NewTaskStatus.FAIL){
                edit1.setText("❗ Не удалось отправить ваше видео на скачивание!");
                sendTelegram.editMessage(edit1);
            }
            return;
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        InlineKeyboardButton original = new InlineKeyboardButton();
        InlineKeyboardButton ruVoice = null;
        InlineKeyboardButton enVoice = null;
        InlineKeyboardButton ukVoice = null;
        original.setText("Оригинальная озвучка");
        String orig = videoDataId+":"+videoData.getAudio_id();
        original.setCallbackData("dv:"+orig);
        buttons.add(List.of(original));
        if(!ru.isEmpty()){
            ruVoice = new InlineKeyboardButton();
            ruVoice.setText("\uD83C\uDDF7\uD83C\uDDFA Русская озвучка");
            String rus = videoDataId+":"+ru;
            ruVoice.setCallbackData("dv:"+rus);
            buttons.add(List.of(ruVoice));
        }
        if(!en.isEmpty()){
            enVoice = new InlineKeyboardButton();
            enVoice.setText("\uD83C\uDDFA\uD83C\uDDF2 Английская озвучка");
            String eng = videoDataId+":"+en;
            enVoice.setCallbackData("dv:"+eng);
            buttons.add(List.of(enVoice));
        }
        if(!uk.isEmpty()){
            ukVoice = new InlineKeyboardButton();
            ukVoice.setText("\uD83C\uDDFA\uD83C\uDDE6 Украинская озвучка");
            String ukr = videoDataId+":"+uk;
            ukVoice.setCallbackData("dv:"+ukr);
            buttons.add(List.of(ukVoice));
        }
        markup.setKeyboard(buttons);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callback.getMessage().getChatId());
        editMessageText.setMessageId(callback.getMessage().getMessageId());
        editMessageText.setText("Выберите озвучку:");
        editMessageText.setReplyMarkup(markup);
        sendTelegram.editMessage(editMessageText);
    }
}
