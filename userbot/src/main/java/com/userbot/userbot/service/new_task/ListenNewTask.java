package com.userbot.userbot.service.new_task;

import com.userbot.userbot.model.Task;
import com.userbot.userbot.service.DownloadVideo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
//Слушаем новые задачи и начинаем их обработку.
public class ListenNewTask {
    private DownloadVideo downloadVideo;
    private ThreadPoolExecutor executorService;
    public ListenNewTask(DownloadVideo downloadVideo,@Value("${bot.pool-task}") int pools){
        this.downloadVideo = downloadVideo;
        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(pools);
    }

    @KafkaListener(
            topicPartitions = @TopicPartition(topic = "${spring.kafka.consumer.topic}", partitions = "${spring.kafka.consumer.partition}"),
            groupId = "${spring.kafka.consumer.group}"
    )
    public void start(Task task, Acknowledgment ack){
        System.out.println(executorService.getPoolSize()+" "+executorService.getActiveCount());
        if(executorService.getActiveCount()<executorService.getMaximumPoolSize()){
            ack.acknowledge();
            executorService.execute(()->{
                log.info("Задача {} принята. Начинаем выполнение... ",task.getVideoDataId());
                try{
                    boolean ok = downloadVideo.start(task);
                    if(!ok){
                        log.warn("Произошла ошибка при выполнении задачи! id:{}",task.getVideoDataId());
                        //topic new error
                        return;
                    }
                    log.info("Успешное выполнение задания: id: {}",task.getVideoDataId());
                } catch (Exception e){
                    log.error("Произошла непредвиденная ошибка при выполнении задачи! id: {}", task.getVideoDataId(), e);
                    //topic new error
                }
            });
        } else {
            log.info("Не удалось начать выполнение задачи. Пул переполнен!");
        }
    }
    public int getCountAvailableTask(){
        return executorService.getMaximumPoolSize()-executorService.getActiveCount();
    }

}
