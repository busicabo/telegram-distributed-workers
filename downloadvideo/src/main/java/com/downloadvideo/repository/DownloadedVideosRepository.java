package com.downloadvideo.repository;

import com.downloadvideo.model.postgresql.DownloadedVideosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DownloadedVideosRepository extends JpaRepository<DownloadedVideosEntity,Long> {
    @Query(value = """
        select downloaded_videos_file_id
        from downloaded_videos
        where downloaded_videos_url = :url
          and downloaded_videos_video_id = :videoId
          and downloaded_videos_audio_id = :audioId
    """,nativeQuery = true)
    String findFileId(
            @Param("url") String url,
            @Param("videoId") String videoId,
            @Param("audioId") String audioId
    );
}
