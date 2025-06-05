package com.example.kafka_alarm_receiver.kafka;

import com.example.kafka_alarm_receiver.domain.AlarmMessage;
import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.es.ElasticsearchService;
import com.example.kafka_alarm_receiver.service.KafkaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerManager implements Observer<KafkaConfig>{

    private final KafkaService kafkaService;
    private final ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory;
    private final ElasticsearchService elasticsearchService;
    private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConfigListener listener;
    public void addListener(KafkaConfig config) {
        String topic = config.getTopic();
        String application = config.getName();

        // 如果该 topic 已经存在监听器，则停止之前的监听器
        if (containers.containsKey(application)) {
            removeListener(application);
            log.info("⚠️ [跳过] 已存在监听器，topic={}，跳过创建", topic);
        }

        try {
            // 配置消息监听器
            ContainerProperties containerProps = createContainerProperties(topic, config);
            ConsumerFactory<String, String> consumerFactory = createConsumerFactory(config);

            // 创建并启动 Kafka 消费者监听容器
            startKafkaListener(application, topic, config, containerProps, consumerFactory);
        } catch (Exception e) {
            log.error("🔥 [失败] 启动 Kafka 监听器失败 | application={} | topic={} | group={}",
                    application, topic, config.getConsumerGroup(), e);
        }
    }

    private ContainerProperties createContainerProperties(String topic, KafkaConfig config) {
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener(createMessageListener(config));
        return containerProps;
    }

    private MessageListener<String, String> createMessageListener(KafkaConfig config) {
        return record -> {
            log.info("📥 [接收] topic={} | key={} | value={}", record.topic(), record.key(), record.value());
            try {
                AlarmMessage alarmMessage = objectMapper.readValue(record.value(), AlarmMessage.class);
                alarmMessage.setDataResource(config.getDataResource());
                alarmMessage.setAppName(config.getName());
                elasticsearchService.saveAlarm(alarmMessage);
                log.info("✅ [写入ES] 告警数据已写入索引");
            } catch (Exception e) {
                log.error("❌ [错误] 解析或写入ES失败", e);
            }
        };
    }

    private void startKafkaListener(String application, String topic, KafkaConfig config,
                                    ContainerProperties containerProps, ConsumerFactory<String, String> consumerFactory) {
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        container.start();
        containers.put(application, container);
        log.info("✅ [启动成功] Kafka 监听器已启动 | topic={} | group={}", topic, config.getConsumerGroup());
    }

    public void removeListener(String application) {
        Optional.ofNullable(containers.get(application))
                .ifPresentOrElse(container -> stopListener(application, container),
                        () -> log.warn("⚠️ [无效操作] 监听器 {} 不存在，无法停止", application));
    }

    private void stopListener(String application, ConcurrentMessageListenerContainer<String, String> container) {
        try {
            container.stop();
            containers.remove(application);
            log.info("🛑 [停止] 已停止监听器：{}", application);
        } catch (Exception e) {
            log.error("💥 [异常] 停止监听器失败：" + application, e);
        }
    }

    public void removeAllListeners() {
        log.info("🧹 [清理] 准备停止所有 Kafka 监听器...");
        containers.forEach(this::stopListener);
        containers.clear();
        log.info("✅ [完成] 所有 Kafka 监听器已成功清理");
    }

    private ConsumerFactory<String, String> createConsumerFactory(KafkaConfig config) {
        Properties properties = kafkaService.createConsumerConfig(config);
        Map<String, Object> configMap = (Map) properties;
        return new DefaultKafkaConsumerFactory<>(configMap);
    }


    @PostConstruct
    public void init() {
        listener.registerObserver(this);
        log.info("🟢 Kafka consumer manager start successfully");
    }

    @Override
    public void onUpdate(KafkaConfig config) {
        log.info("Kafka consumer manager detected a configuration change and updated the consumer.");
        addListener(config);
    }

    @Override
    public void onInit(KafkaConfig config) {
        this.onUpdate(config);
    }
}
