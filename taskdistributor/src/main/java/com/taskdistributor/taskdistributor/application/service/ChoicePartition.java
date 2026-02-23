package com.taskdistributor.taskdistributor.application.service;

import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/*
Раз в секунду отправляет количество свободных задач и свою партицию кафка в редис,
что бы распределитель выбирал наиболее свободного бота.
 */

public class ChoicePartition {


    private final RedisCommands<String, String> redis;

    public ChoicePartition(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    public Integer pickBestPartition() {
        List<String> parts = redis.zrevrange("part:rank", 0, 4);
        if (parts.isEmpty()) return null;

        for (String part : parts) {
            int partInt;
            try {
                partInt = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                log.warn("В part:rank лежит не число: {}", part);
                redis.zrem("part:rank", part);
                continue;
            }

            String capKey = "part:cap:" + part;
            String capStr = redis.get(capKey);

            // ключа нет -> сервис считаем мёртвым/неинициализированным
            if (capStr == null) {
                log.warn("Нет ключа {} — удаляю {} из part:rank", capKey, part);
                redis.zrem("part:rank", part);
                continue;
            }

            int cap;
            try {
                cap = Integer.parseInt(capStr);
            } catch (NumberFormatException e) {
                log.warn("В {} лежит не число: {}", capKey, capStr);
                redis.zrem("part:rank", part);
                continue;
            }
            if (cap < 1) {
                continue;
            }
            log.info("Свободный сервис найден! partition={}", partInt);
            return partInt;
        }
        return null;
    }
    public void successfulTask(int partition){
        redis.set("part:cap:"+partition,
                String.valueOf(Integer.parseInt(redis.get("part:cap:"+partition))-1));
    }
}
