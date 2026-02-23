package com.userbot.userbot.adapters.out.messaging.kafka;

import com.userbot.userbot.application.port.out.StatusPublisherPort;
import com.userbot.userbot.domain.model.InfoVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStatusPublisher implements StatusPublisherPort {

    @Qualifier("kafkaTemplateInfoVideo")
    private final KafkaTemplate<String, InfoVideo> kafkaTemplate;

    @Value("${spring.kafka.producer.topic-info-video}")
    private String topic;

    @Override
    public void publish(InfoVideo infoVideo) {
        kafkaTemplate.send(topic, infoVideo);
        log.info("Статус отправлен! type: {}, id: {}", infoVideo.getType(), infoVideo.getVideoDataId());
    }
}
