package com.downloadvideo.repository;

import com.downloadvideo.model.postgresql.VideoDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VideoDataRepository extends JpaRepository<VideoDataEntity,Long> {
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE video_data
            SET video_data_process = :status
            WHERE video_data_id = :id
            """,nativeQuery = true)
    int completionProcess(@Param("id")Long id,@Param("status") String status);

    @Query(value= """
            SELECT video_data_id FROM video_data
            WHERE video_data_process = 'DOWNLOAD'
            AND user_id = :id
            AND video_data_created_at BETWEEN NOW() - INTERVAL '1 minutes' AND NOW()
            ORDER BY video_data_created_at DESC
            LIMIT 1
            """,nativeQuery = true)
    Long checkExistsVideo(@Param("id") Long userId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE video_data
            SET video_data_start_download = now()
            WHERE video_data_id = :id
            """,nativeQuery = true)
    void setDownloadTimeNow(@Param("id")Long id);

    @Query(value = "SELECT DISTINCT ON (user_id) video_data_chat_id FROM video_data",nativeQuery = true)
    List<String> getChats();

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE video_data
            SET video_data_finished = now()
            WHERE video_data_id = :id
            """,nativeQuery = true)
    void setFinishTimeNow(@Param("id")Long id);
}
