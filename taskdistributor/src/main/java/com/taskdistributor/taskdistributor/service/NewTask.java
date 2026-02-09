package com.taskdistributor.taskdistributor.service;

import com.taskdistributor.taskdistributor.model.InfoVideo;
import com.taskdistributor.taskdistributor.model.InfoVideoType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import com.taskdistributor.taskdistributor.model.Task;

@Slf4j
@Service
//Принимаем или отклоняем новую задачу, в зависимости заполнена очередь или же нет.
public class NewTask {
    @Autowired
    private SendStatus sendStatus;
    @Autowired
    private QueueTask queueTask;

    @KafkaListener(topics="${spring.kafka.consumer.topic}")
    public void start(Task task,Acknowledgment ack){
        ack.acknowledge();
        if(!queueTask.addTask(task)){
            log.info("Бот нагружен! Не смогли добавить новую задачу в очередь!");
            sendStatus.sendStatus(
                    new InfoVideo(task.getVideoDataId(), "Бот нагружен! Пожалуйста попробуйте еще раз или подождите некоторое время!", InfoVideoType.ERROR));
        } else{
            sendStatus.sendStatus(new InfoVideo(task.getVideoDataId(),"Видео добавлено в очередь. Ожидайте...",InfoVideoType.INFO));
        }
    }
}
