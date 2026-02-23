package com.userbot.userbot.infrastructure.config;

import com.userbot.userbot.domain.model.InfoVideo;
import com.userbot.userbot.domain.model.Task;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;
    @Value("${spring.kafka.consumer.group}")
    private String group;
    @Bean
    public ConsumerFactory<String, Task> consumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<Task> valueDeserializer = new JsonDeserializer<>(Task.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setRemoveTypeHeaders(true);
        valueDeserializer.setUseTypeMapperForKey(false);
        valueDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,Task> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,Task> containerFactory=new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory());
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0L));
        containerFactory.setCommonErrorHandler(errorHandler);
        return containerFactory;
    }

    @Bean
    public ProducerFactory<String, InfoVideo> producerFactoryVideoInfo(){
        Map<String,Object> map = new HashMap<>();
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        map.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        map.put(ProducerConfig.ACKS_CONFIG, "all");
        map.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 3);
        map.put("spring.json.add.type.headers", false);

        map.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10_000);
        map.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 20_000);
        return new DefaultKafkaProducerFactory<>(map);
    }

    @Bean("kafkaTemplateInfoVideo")
    public KafkaTemplate<String,InfoVideo> kafkaTemplateInfoVideo(){
        return new KafkaTemplate<>(producerFactoryVideoInfo());
    }
}
