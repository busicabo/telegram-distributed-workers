package com.downloadvideo.service.handler;

import com.downloadvideo.config.bot.MainBot;
import com.downloadvideo.model.DownloadProcess;
import com.downloadvideo.model.event.MessageEvent;
import com.downloadvideo.service.SendTelegram;
import com.downloadvideo.service.downloads.YoutubeQualityDownload;
import com.downloadvideo.model.ResponseDto;
import com.downloadvideo.model.postgresql.VideoDataEntity;
import com.downloadvideo.model.YoutubeQuality;
import com.downloadvideo.service.VideoDataService;
import com.downloadvideo.service.handler.main.MessageHandlers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class YoutubeMessageHandler implements MessageHandlers {
/*
Обрабатывает все видео с ютуба. Возращает пользователю название ролика и доступные качества.
За поиск доступныз качеств отвечает YoutubeQualityDownload.
Так же это начальная точка создание задачи.
Каждая задача обрабатываеться асинхроно, что бы не блокировать поток.
 */
    @Autowired
    private VideoDataService videoDataService;
    @Autowired
    private YoutubeQualityDownload youtubeQualityDownload;
    @Autowired
    private SendTelegram sendTelegram;
    //Асинхроно выполняем задачу. Если бот переполненЮ ничего не отвечаем пользователю(знаю что нужно исправить).
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            0L,
            java.util.concurrent.TimeUnit.MILLISECONDS,
            // очередь на 0 => вообще без очереди (жестко: либо сразу выполняется, либо отказ)
            new java.util.concurrent.SynchronousQueue<>(),
            new ThreadPoolExecutor.DiscardPolicy() // молча отбросить
    );

    List<String> urls = List.of("https://www.youtube.com/watch?","https://youtu.be","https://youtube.com/watch?","https://youtube.com/shorts","https://www.youtube.com/shorts","https://www.youtube.com/live");
    @Override
    public boolean isValid(String message) {
        for(String url: urls){
            if(message.startsWith(url)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void handle(MessageEvent event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                Update update = event.getUpdate();;
                String message = update.getMessage().getText();
                SendMessage search = new SendMessage(String.valueOf(update.getMessage().getChatId()),
                        "\uD83D\uDD0D Начинаем поиск...");
                Message searchMessage = sendTelegram.sendMessage(search);
                if(searchMessage==null){
                    for(int i = 0;i<5;i++){
                        searchMessage=sendTelegram.sendMessage(search);
                        if(searchMessage!=null){
                            break;
                        }
                    }
                }
                if(searchMessage==null){
                    return;
                }
                ResponseDto responseDto = youtubeQualityDownload.giveQualityDownload(message);
                if(responseDto==null){
                    EditMessageText sendMessage = new EditMessageText();
                    sendMessage.setChatId(searchMessage.getChatId());
                    sendMessage.setMessageId(searchMessage.getMessageId());
                    sendMessage.setText("❗ Я не смог загрузить доступные качества видео!");
                    sendTelegram.editMessage(sendMessage);
                    log.warn("Походу произошла ошибка при попытке получить качества видео!");
                    return;
                }
                if(responseDto.getYoutubeQuality().isEmpty()){
                    EditMessageText sendMessage = new EditMessageText();
                    sendMessage.setChatId(searchMessage.getChatId());
                    sendMessage.setMessageId(searchMessage.getMessageId());
                    sendMessage.setText("❗ Видео не было найдено! Лимит 2гб.");
                    sendTelegram.editMessage(sendMessage);
                    log.warn("Видео не было найдено!");
                    return;
                }
                EditMessageText sendMessage = new EditMessageText();
                sendMessage.setChatId(searchMessage.getChatId());
                sendMessage.setMessageId(searchMessage.getMessageId());
                sendMessage.setText(
                        "\uD83D\uDCFD\uFE0F "+responseDto.getVideoName()+"\n"+"По ссылке: "+message);
                YoutubeQuality youtubeQuality = responseDto.getYoutubeQuality().getFirst();

                VideoDataEntity videoDataSave = new VideoDataEntity();
                videoDataSave.setDuration(responseDto.getDuration());
                videoDataSave.setUser_id(update.getMessage().getFrom().getId());
                videoDataSave.setUrl(message);
                videoDataSave.setAudio_id(youtubeQuality.getMusicId());
                videoDataSave.setChat_id(update.getMessage().getChatId());
                videoDataSave.setProcess(DownloadProcess.INFO);
                videoDataSave.setVideoName(responseDto.getVideoName());
                if(youtubeQuality.getTranslateVoice().get("ru")!=null){
                    videoDataSave.setRu_id(youtubeQuality.getTranslateVoice().get("ru"));
                }
                if(youtubeQuality.getTranslateVoice().get("en")!=null){
                    videoDataSave.setEn_id(youtubeQuality.getTranslateVoice().get("en"));
                }
                VideoDataEntity videoData = videoDataService.saveVideoData(videoDataSave);
                if(videoData==null){
                    EditMessageText messageError = new EditMessageText();
                    messageError.setChatId(searchMessage.getChatId());
                    messageError.setMessageId(searchMessage.getMessageId());
                    messageError.setText("❗ Ошибка при сохранении результатов! Попробуйте еще раз!");
                    sendTelegram.editMessage(messageError);
                    return;
                }
                List<List<InlineKeyboardButton>> inlineList = getLists(responseDto, videoData);
                InlineKeyboardMarkup inline = InlineKeyboardMarkup.builder().keyboard(inlineList).build();
                sendMessage.setReplyMarkup(inline);
                sendTelegram.editMessage(sendMessage);


            }

            private static List<List<InlineKeyboardButton>> getLists(ResponseDto responseDto, VideoDataEntity videoData) {
                List<List<InlineKeyboardButton>> inlineList = new ArrayList<>();
                List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
                int qualityCount=0;
                for(YoutubeQuality quality: responseDto.getYoutubeQuality()){
                    if(qualityCount%2==0 && !inlineKeyboardButtons.isEmpty()){
                        inlineList.add(inlineKeyboardButtons);
                        inlineKeyboardButtons = new ArrayList<>();
                    }
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText("\uD83D\uDCFD\uFE0F: "+quality.getFormat_note()+", "+quality.getSize()+"Mb");
                    String data= videoData.getId()+":"+ videoData.getUser_id() +":"+quality.getVideoId()+":"
                            +quality.getSize()+":"+quality.getFormatNote();
                    String inlineData = "qy:"+data;
                    button.setCallbackData(inlineData);
                    inlineKeyboardButtons.add(button);
                    qualityCount++;
                }
                if(!inlineKeyboardButtons.isEmpty()){
                    inlineList.add(inlineKeyboardButtons);
                }
                return inlineList;
            }
        });
    }
}
