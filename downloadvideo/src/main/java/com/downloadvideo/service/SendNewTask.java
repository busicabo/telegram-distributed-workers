package com.downloadvideo.service;

import com.downloadvideo.model.NewTaskStatus;
import com.downloadvideo.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

// Отправка задача на распределитель задач.
@Service
@Slf4j
public class SendNewTask {
    @Value("${spring.kafka.producer.topic}")
    private String topic;
    @Autowired
    private KafkaTemplate<String,Task> kafkaTemplate;

    public NewTaskStatus start(Task task){
        try {
            kafkaTemplate.send(topic,task);
            log.info("Новая задача успешно добавлена!");
            return NewTaskStatus.OK;
        } catch (Exception e){
            log.error("Ошибка при отправке события на загрузку! id:{}",task.getVideoDataId(),e);
            return NewTaskStatus.FAIL;
        }
    }
}
