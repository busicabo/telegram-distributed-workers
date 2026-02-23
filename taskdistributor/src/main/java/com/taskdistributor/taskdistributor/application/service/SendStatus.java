package com.taskdistributor.taskdistributor.application.service;

import com.taskdistributor.taskdistributor.domain.model.InfoVideo;
import com.taskdistributor.taskdistributor.domain.model.InfoVideoType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
//Отправляет статус
public class SendStatus {
    @Autowired
    @Qualifier("templateInfoVideo")
    private KafkaTemplate<String, InfoVideo> kafkaTemplate;
    @Value("${spring.kafka.producer.topic-info-video}")
    private String topic;

    public void sendStatus(InfoVideo infoVideo){
        kafkaTemplate.send(topic,infoVideo);
        log.info("Статус успешно отправлен! type: {}, id: {}",infoVideo.getType(),infoVideo.getVideoDataId());
    }
}
