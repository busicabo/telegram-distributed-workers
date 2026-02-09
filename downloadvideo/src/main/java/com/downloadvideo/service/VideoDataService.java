package com.downloadvideo.service;

import com.downloadvideo.model.DownloadProcess;
import com.downloadvideo.model.postgresql.VideoDataEntity;
import com.downloadvideo.repository.VideoDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoDataService {
    @Autowired
    private VideoDataRepository videoDataRepository;

    @Transactional
    public VideoDataEntity saveVideoData(VideoDataEntity videoData){
        return videoDataRepository.save(videoData);
    }

    public VideoDataEntity getVideoData(Long id){
        return videoDataRepository.findById(id).orElse(null);
    }

    @Transactional
    public void completionProcess(Long videoDataId, DownloadProcess process){
        if(process==DownloadProcess.DOWNLOAD){
            videoDataRepository.setDownloadTimeNow(videoDataId);
        } else if(process==DownloadProcess.FINISH){
            videoDataRepository.setFinishTimeNow(videoDataId);
        }
        videoDataRepository.completionProcess(videoDataId,process.toString());
    }
    public boolean checkExistsTask(Long userId){
        return videoDataRepository.checkExistsVideo(userId) == null;
    }

    public List<String> getUserChats(){
        return videoDataRepository.getChats();
    }
}
