package com.example.kafka_alarm_receiver.kafka;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer 接口用于定义观察者模式的更新行为。
 */
interface Observer<T> {
    void onUpdate(T config); // 当配置更新时触发
    void onInit(T config);   // 当观察者注册时初始化配置
}

/**
 * Listener 接口用于定义监听器的行为，包括注册、移除和通知观察者。
 */
interface Listener<T> {
    void registerObserver(Observer<T> o); // 注册观察者
    void removeObserver(Observer<T> o);   // 移除观察者
    void notifyObservers(T config);       // 通知所有观察者配置已更新
}

/**
 * ConfigListener 是一个 Spring 组件，负责监听 Kafka 配置的变化，并通过观察者模式通知其他组件。
 * 它使用 Redis 和数据库进行配置同步。
 */
@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class ConfigListener implements Listener<KafkaConfig> {

    // 观察者列表（线程安全）
    private final List<Observer<KafkaConfig>> observerRegistry = new CopyOnWriteArrayList<>();
    // 本地配置缓存
    private final Map<String, KafkaConfig> configRegistry = new HashMap<>();
    // Kafka 配置服务
    private final KafkaConfigService kafkaConfigService;
    // Redis 模板
    private final StringRedisTemplate stringRedisTemplate;
    // JSON 序列化/反序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Redis 中存储 Kafka 配置的键名
    private final static String REDIS_KEY = "kafka_alarm_configs";

    /**
     * 注册一个观察者。
     * 会为新注册的观察者调用方法以初始化当前配置。
     */
    @Override
    public void registerObserver(Observer<KafkaConfig> o) {
        observerRegistry.add(o);
        for (Map.Entry<String, KafkaConfig> entry: configRegistry.entrySet()) {
            o.onInit(entry.getValue());
        }
    }

    /**
     * 移除一个观察者。
     */
    @Override
    public void removeObserver(Observer<KafkaConfig>  o) {
        observerRegistry.remove(o);
    }

    /**
     * 通知所有观察者配置已更新。
     */
    @Override
    public void notifyObservers(KafkaConfig config) {
        for (Observer<KafkaConfig> o : observerRegistry) {
            o.onUpdate(config);
        }
    }

    /**
     * 定时任务，每 5 秒执行一次，用于检测 Kafka 配置是否发生变化。
     */
    @Scheduled(fixedRate = 5000)
    public void poll() {
        try {
            log.info("start check kafka connect config");
            // 1. 从 Redis 中获取当前配置
            Map<String , KafkaConfig> redisConfigs = trySyncConfigsFromRedis();
            // 2. 从数据库中获取当前配置
            Map<String, KafkaConfig> databaseConfigs = trySyncConfigsFromPG();

            if (!redisConfigs.equals(databaseConfigs)) {
                // 如果 Redis 和数据库中的配置不一致，则将数据库中的配置同步到 Redis
                log.info("Kafka configuration listener detected a configuration change and started synchronizing the configuration.");
                syncConfigToRedis(databaseConfigs);
            }

            if (!configRegistry.equals(databaseConfigs)) {
                // 如果本地缓存与数据库中的配置不一致，则更新本地缓存并通知所有观察者
                syncConfigToLocal(databaseConfigs);
                for (KafkaConfig config : configRegistry.values()) {
                    notifyObservers(config);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("parse kafka alarm config error {}", e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * 尝试从 Redis 中读取 Kafka 配置。
     */
    private Map<String , KafkaConfig> trySyncConfigsFromRedis() throws JsonProcessingException {
        HashMap<String , KafkaConfig> configs = new HashMap<>();
        Map<Object, Object> kafkaAlarmConfigs = stringRedisTemplate.opsForHash().entries(REDIS_KEY);
        if (kafkaAlarmConfigs.size() != 0) {
            for (Map.Entry<Object, Object> entry : kafkaAlarmConfigs.entrySet()) {
                String content = (String) entry.getValue();
                KafkaConfig config = objectMapper.readValue(content, KafkaConfig.class);
                configs.put(config.getName(), config);
            }
        }
        return configs;
    }

    /**
     * 尝试从数据库中读取 Kafka 配置。
     */
    private Map<String , KafkaConfig> trySyncConfigsFromPG() throws JsonProcessingException {
        HashMap<String , KafkaConfig> configs = new HashMap<>();
        for (KafkaConfig config : kafkaConfigService.list()) {
            configs.put(config.getName(), config);
        }
        return configs;
    }

    /**
     * 将数据库中的 Kafka 配置同步到 Redis。
     */
    private void syncConfigToRedis(Map<String, KafkaConfig> configs) throws JsonProcessingException {
        stringRedisTemplate.delete(REDIS_KEY);
        for (Map.Entry<String, KafkaConfig> entry : configs.entrySet()) {
            String content = objectMapper.writeValueAsString(entry.getValue());
            stringRedisTemplate.opsForHash().put(REDIS_KEY, entry.getKey(), content);
        }
    }

    /**
     * 将数据库中的 Kafka 配置同步到本地缓存。
     */
    private void syncConfigToLocal(Map<String, KafkaConfig> configs) throws JsonProcessingException {
        configRegistry.clear();
        configRegistry.putAll(configs);
    }
}
