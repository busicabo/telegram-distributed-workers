package com.downloadvideo.service;

import com.downloadvideo.model.Task;
import com.downloadvideo.model.postgresql.DownloadedVideosEntity;
import com.downloadvideo.repository.DownloadedVideosRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DownloadedVideosService {
    @Autowired
    private DownloadedVideosRepository downloadedVideosRepository;

    public String getFileId(String url,String videoId, String audioId){
        return downloadedVideosRepository.findFileId(url,videoId,audioId);
    }
    public String getFileId(Task task){
        return downloadedVideosRepository.findFileId(task.getUrl(),task.getVideoId(),task.getAudioId());
    }

    @Transactional
    public DownloadedVideosEntity save(DownloadedVideosEntity videosEntity){
        try{
            return downloadedVideosRepository.save(videosEntity);
        }catch (Exception e){
            log.error("Ошибка при сохранение видео!",e);
            return null;
        }
    }
}
