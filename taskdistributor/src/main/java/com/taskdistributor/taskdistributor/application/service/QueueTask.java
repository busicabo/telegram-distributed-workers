package com.taskdistributor.taskdistributor.application.service;

import com.taskdistributor.taskdistributor.domain.model.Task;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
@Slf4j
/*
 Храниться очередь всех задач. Если очередь не пуста, то раз в секунду проверяет
 доступен ли бот для выполнения задачи
 */
public class QueueTask {
    private ChoicePartition choicePartition;
    private BlockingQueue<Task> blockingQueue;
    private SendStatus sendStatus;
    private SendNewTaskVideo sendNewTaskVideo;
    public QueueTask( @Value("${queue-size}") int size, ChoicePartition choicePartition, SendStatus status,SendNewTaskVideo sendNewTaskVideo){
        this.sendNewTaskVideo=sendNewTaskVideo;
        this.sendStatus=status;
        this.choicePartition=choicePartition;
        blockingQueue = new ArrayBlockingQueue<>(size);
    }

    public boolean addTask(Task task){
        return blockingQueue.offer(task);
    }

    public Task getTask(){
        return blockingQueue.poll();
    }

    @Scheduled(fixedRate = 1000)
    public void check(){
        while(true){
            if(blockingQueue.isEmpty()) return;
            Integer partition = choicePartition.pickBestPartition();
            if(partition!=null){
                Task task = getTask();
                if(task==null) return;
                sendNewTaskVideo.send(task,partition);
                log.info("Задача успешно отправлена! id:{}",task.getVideoDataId());
                choicePartition.successfulTask(partition);
            } else {
                return;
            }
        }
    }
}
