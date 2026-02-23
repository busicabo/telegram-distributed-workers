package com.userbot.userbot.application.service;

import com.userbot.userbot.adapters.in.messaging.kafka.ListenNewTask;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
/*
Бот каждую секунду отправляет статус с количеством свободных задач и своей партицией кафка в редис.
Это нужно что бы давать задачу самому менее нагруженому боту
 */

public class BotStatusSender {
    @Autowired
    private RedisCommands<String, String> redis;
    @Autowired
    private ListenNewTask listenNewTask;
    @Value("${spring.kafka.consumer.partition}")
    private int partition;

    @Scheduled(fixedRate = 1000)
    public void sendStatus() {
        int capacity = getCurrentCapacity();
        sendToRedis(partition, capacity);
    }

    private void sendToRedis(int partition, int capacity) {
        redis.set(
                "part:cap:" + partition,
                String.valueOf(capacity),
                SetArgs.Builder.px(2000)
        );
        redis.zadd(
                "part:rank",
                capacity,
                String.valueOf(partition)
        );
    }

    private int getCurrentCapacity() {
        return listenNewTask.getCountAvailableTask();
    }
}
