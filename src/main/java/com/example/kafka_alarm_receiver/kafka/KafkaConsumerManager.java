
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

/**
 * KafkaConsumerManager 是一个 Spring 组件，用于管理 Kafka 消费者的生命周期。
 * 它实现了 `Observer<KafkaConfig>` 接口，用于监听 Kafka 配置的变化，并根据最新的配置创建或更新 Kafka 消费者。
 */
@SuppressWarnings("ALL")
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerManager implements Observer<KafkaConfig> {

    // Kafka 服务，提供消费者配置的构建
    private final KafkaService kafkaService;
    // Kafka 监听器容器工厂，用于创建 Kafka 消费者容器
    private final ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory;
    // Elasticsearch 服务，用于将接收到的告警信息写入 ES
    private final ElasticsearchService elasticsearchService;
    // 存储所有 Kafka 消费者容器的映射（key: 应用名）
    private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers = new ConcurrentHashMap<>();
    // JSON 序列化/反序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();
    // ConfigListener 实例，用于注册观察者并监听配置变化
    private final ConfigListener listener;

    /**
     * 添加一个新的 Kafka 消费者监听器。
     * 如果该应用名已经存在监听器，则先移除旧的监听器。
     */
    public void addListener(KafkaConfig config) {
        String topic = config.getTopic();          // 获取 Kafka 主题
        String application = config.getName();     // 获取应用名作为唯一标识

        // 如果该应用名已经存在监听器，则移除旧的监听器
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

    /**
     * 创建 Kafka 容器属性，指定监听的主题和消息处理器。
     */
    private ContainerProperties createContainerProperties(String topic, KafkaConfig config) {
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener(createMessageListener(config));
        return containerProps;
    }

    /**
     * 创建 Kafka 消息监听器，处理接收到的消息。
     */
    private MessageListener<String, String> createMessageListener(KafkaConfig config) {
        return record -> {
            log.info("📥 [接收] topic={} | key={} | value={}", record.topic(), record.key(), record.value());
            try {
                // 解析消息为 AlarmMessage 对象
                AlarmMessage alarmMessage = objectMapper.readValue(record.value(), AlarmMessage.class);
                // 设置额外的业务字段
                alarmMessage.setDataResource(config.getDataResource());
                alarmMessage.setAppName(config.getName());
                // 将告警信息保存到 Elasticsearch
                elasticsearchService.saveAlarm(alarmMessage);
                log.info("✅ [写入ES] 告警数据已写入索引");
            } catch (Exception e) {
                log.error("❌ [错误] 解析或写入ES失败", e);
            }
        };
    }

    /**
     * 创建并启动 Kafka 消费者监听容器。
     */
    private void startKafkaListener(String application, String topic, KafkaConfig config,
                                    ContainerProperties containerProps, ConsumerFactory<String, String> consumerFactory) {
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        container.start();
        containers.put(application, container);
        log.info("✅ [启动成功] Kafka 监听器已启动 | topic={} | group={}", topic, config.getConsumerGroup());
    }

    /**
     * 移除指定应用名的 Kafka 消费者监听器。
     */
    public void removeListener(String application) {
        Optional.ofNullable(containers.get(application))
                .ifPresentOrElse(container -> stopListener(application, container),
                        () -> log.warn("⚠️ [无效操作] 监听器 {} 不存在，无法停止", application));
    }

    /**
     * 停止指定应用名的 Kafka 消费者监听器。
     */
    private void stopListener(String application, ConcurrentMessageListenerContainer<String, String> container) {
        try {
            container.stop();
            containers.remove(application);
            log.info("🛑 [停止] 已停止监听器：{}", application);
        } catch (Exception e) {
            log.error("💥 [异常] 停止监听器失败：" + application, e);
        }
    }

    /**
     * 清理所有 Kafka 消费者监听器。
     */
    public void removeAllListeners() {
        log.info("🧹 [清理] 准备停止所有 Kafka 监听器...");
        containers.forEach(this::stopListener);
        containers.clear();
        log.info("✅ [完成] 所有 Kafka 监听器已成功清理");
    }

    /**
     * 根据 Kafka 配置创建 Kafka 消费者工厂。
     */
    private ConsumerFactory<String, String> createConsumerFactory(KafkaConfig config) {
        Properties properties = kafkaService.createConsumerConfig(config);
        Map<String, Object> configMap = (Map) properties;
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    /**
     * 初始化方法，在 Bean 构造完成后注册自己为 ConfigListener 的观察者。
     */
    @PostConstruct
    public void init() {
        listener.registerObserver(this);
        log.info("🟢 Kafka consumer manager start successfully");
    }

    /**
     * 当 Kafka 配置更新时触发的方法。
     */
    @Override
    public void onUpdate(KafkaConfig config) {
        log.info("Kafka consumer manager detected a configuration change and updated the consumer.");
        addListener(config);
    }

    /**
     * 当 Kafka 配置初始化时触发的方法。
     */
    @Override
    public void onInit(KafkaConfig config) {
        this.onUpdate(config);
    }
}