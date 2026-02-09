CREATE TABLE IF NOT EXISTS users (
  user_id bigint PRIMARY KEY,
  user_username varchar(35),
  user_register timestamptz DEFAULT now(),
  user_count_download int DEFAULT 0,
  user_role varchar(60) DEFAULT 'USER',
  user_blocked boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS video_data (
  video_data_id bigserial PRIMARY KEY,

  user_id bigint NOT NULL
    REFERENCES users(user_id) ON DELETE CASCADE,

  video_data_chat_id bigint,
  video_data_message_id bigint,

  video_data_url text NOT NULL,
  video_data_height varchar(30),
  video_data_size bigint,
  video_data_video_id varchar(30),
  video_data_audio_id varchar(20),
  video_data_audio_ru_id varchar(20),
  video_data_audio_en_id varchar(20),
  video_data_process varchar(30) DEFAULT 'INFO',

  video_data_created_at timestamptz DEFAULT now(),
  video_data_finished timestamptz
);

CREATE TABLE IF NOT EXISTS downloaded_videos (
  downloaded_videos_id bigserial PRIMARY KEY,

  downloaded_videos_url text NOT NULL,
  downloaded_videos_video_id text NOT NULL,
  downloaded_videos_audio_id text NOT NULL,

  video_data_id bigint REFERENCES video_data(video_data_id),

  downloaded_videos_file_id text NOT NULL,
  downloaded_videos_size bigint,

  downloaded_videos_created_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT downloaded_videos_unique UNIQUE
    (downloaded_videos_url, downloaded_videos_video_id, downloaded_videos_audio_id)
);