package com.downloadvideo.model.postgresql;

import com.downloadvideo.model.DownloadProcess;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="video_data")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class VideoDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="video_data_id")
    private Long id;
    @Column(name="user_id")
    private Long user_id;
    @Column(name="video_data_chat_id")
    private Long chat_id;
    @Column(name="video_data_message_id")
    private Long message_id;
    @Column(name="video_data_video_id")
    private String video_id;
    @Column(name="video_data_url")
    private String url;
    @Column(name="video_data_size")
    private Long size;
    @Column(name="video_data_format_note")
    private String format_note;
    @Column(name="video_data_audio_id")
    private String audio_id;
    @Column(name="video_data_process")
    @Enumerated(value = EnumType.STRING)
    private DownloadProcess process;
    @Column(name="video_data_audio_ru_id")
    private String ru_id;
    @Column(name="video_data_audio_en_id")
    private String en_id;
    @Column(name="video_data_created_at",
            insertable = false,
            updatable = false)
    private OffsetDateTime created_at;
    @Column(name="video_data_finished")
    private OffsetDateTime finished;
    @Column(name="video_data_duration")
    private int duration;
    @Column(name="video_data_start_download")
    private OffsetDateTime start_download;
    @Column(name="video_data_name")
    private String videoName;
}
