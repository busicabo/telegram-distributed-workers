package com.downloadvideo.infrastructure.config;

import com.downloadvideo.domain.model.InfoVideo;
import com.downloadvideo.domain.model.Task;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
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
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Task> producerFactory() {
        Map<String, Object> props = new HashMap<>();

        // --- базовое ---
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        // --- надёжность ---
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 3);
        props.put("spring.json.add.type.headers", false);

        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 20_000);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Task> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Value("${spring.kafka.consumer.group-info-video}")
    private String groupInfoVideo;

    @Bean("consumerFactoryVideoInfo")
    public ConsumerFactory<String, InfoVideo> consumerFactoryVideoInfo() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupInfoVideo);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // (опционально) чтобы при первом старте читать с начала
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<InfoVideo> valueDeserializer = new JsonDeserializer<>(InfoVideo.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setRemoveTypeHeaders(true);
        valueDeserializer.setUseTypeMapperForKey(false);
        valueDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean("kafkaListenerContainerFactoryVideoInfo")
    public ConcurrentKafkaListenerContainerFactory<String, InfoVideo> kafkaListenerContainerFactoryVideoInfo() {
        ConcurrentKafkaListenerContainerFactory<String, InfoVideo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactoryVideoInfo());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0L));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}