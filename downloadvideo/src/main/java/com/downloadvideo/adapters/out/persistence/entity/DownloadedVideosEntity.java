package com.downloadvideo.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="downloaded_videos")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class DownloadedVideosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "downloaded_videos_id")
    private Long id;
    @Column(name = "downloaded_videos_url")
    private String url;
    @Column(name = "downloaded_videos_video_id")
    private String videoId;
    @Column(name = "downloaded_videos_audio_id")
    private String audioId;
    @Column(name = "downloaded_videos_file_id")
    private String fileId;

    public DownloadedVideosEntity(String url, String videoId, String audioId, String fileId, Long videoDataId) {
        this.url = url;
        this.videoId = videoId;
        this.audioId = audioId;
        this.fileId = fileId;
        this.videoDataId = videoDataId;
    }

    @Column(name = "video_data_id")
    private Long videoDataId;
    @Column(name = "downloaded_videos_created_at",
            insertable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    public DownloadedVideosEntity(String url, String videoId, String audioId, String fileId) {
        this.url = url;
        this.videoId = videoId;
        this.audioId = audioId;
        this.fileId = fileId;
    }
}
