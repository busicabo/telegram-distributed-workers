package com.taskdistributor.taskdistributor.service;

import com.taskdistributor.taskdistributor.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
//Отправляет задачу определенному боту.
public class SendNewTaskVideo {
    @Autowired
    @Qualifier("template")
    private KafkaTemplate<String, Task> kafkaTemplate;
    @Value("${spring.kafka.producer.topic}")
    private String topic;

    public void send(Task task,Integer partition){
        kafkaTemplate.send(topic,partition,null,task);
    }
}
