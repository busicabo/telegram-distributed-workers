package com.userbot.userbot.service;

import com.userbot.userbot.model.DownloadPath;
import com.userbot.userbot.model.InfoVideo;
import com.userbot.userbot.model.InfoVideoType;
import com.userbot.userbot.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
@Slf4j
//Главный класс по скачиванию и отправке видео. Управляет всеми нужными процесами для выполнения задачаи.
public class DownloadVideo {
    @Autowired
    private SendVideoTelegram sendVideoTelegram;
    @Autowired
    private DownloadVideoToYourDevice downloadVideoToYourDevice;
    @Autowired
    private SendStatus sendStatus;
    @Autowired
    private DownloadPreview downloadPreview;
    private String path = DownloadPath.PATH;
    public boolean start(Task task){
        try{

            boolean download = downloadVideoToYourDevice.download(task);
            if(download){
                boolean downloadPreviewCheck = downloadPreview.downloadPreview(task.getUrl(),task.getVideoDataId());
                if(downloadPreviewCheck){
                    log.info("Успешная загрузка превью! id:{}",task.getVideoDataId());
                } else {
                    log.info("Превью не было загружено! id:{}",task.getVideoDataId());
                }
                boolean sendTelegram = sendVideoTelegram.videoDownloadProcess(task);
                if(sendTelegram){
                    log.info("Задача прошла успешно! id: {}",task.getVideoDataId());
                    return true;
                }
            }
            log.info("Задача не прошла успешно :( ! id:{}",task.getVideoDataId());
            sendStatus.sendStatus(new InfoVideo(task.getVideoDataId(),"Возникла ошибка при скачивании видео", InfoVideoType.ERROR));
            return false;

        } catch(Exception e){
            log.error("Неудачное скачивание :(",e);
            sendStatus.sendStatus(new InfoVideo(task.getVideoDataId(),"Возникла ошибка при скачивании видео", InfoVideoType.ERROR));
            return false;
        }
        finally {
            File file = new File(path);
            File[] files = file.listFiles();
            if(files!=null && files.length!=0){
                for(File delFile: files){
                    if(delFile.getName().startsWith(task.getVideoDataId().toString())){
                        delFile.delete();
                    }
                }
            }
        }
    }


}
