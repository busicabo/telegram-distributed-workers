package com.downloadvideo.application.service;

import com.downloadvideo.domain.model.DownloadProcess;
import com.downloadvideo.adapters.out.persistence.entity.VideoDataEntity;
import com.downloadvideo.adapters.out.persistence.repository.VideoDataRepository;
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
