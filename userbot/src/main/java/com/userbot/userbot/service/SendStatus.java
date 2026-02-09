package com.userbot.userbot.service;

import com.userbot.userbot.model.InfoVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
//Отправка статусов
public class SendStatus {
    @Autowired
    @Qualifier("kafkaTemplateInfoVideo")
    private KafkaTemplate<String,InfoVideo> kafkaTemplate;
    @Value("${spring.kafka.producer.topic-info-video}")
    private String topic;

    public void sendStatus(InfoVideo infoVideo){
        kafkaTemplate.send(topic,infoVideo);
        log.info("Статус отправлен! type: {}, id: {}",infoVideo.getType(),infoVideo.getVideoDataId());
    }
}
