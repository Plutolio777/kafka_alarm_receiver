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

interface Observer<T> {
    void onUpdate(T config);
    void onInit(T config);
}

interface Listener<T> {
    void registerObserver(Observer<T> o);
    void removeObserver(Observer<T> o);
    void notifyObservers(T config);
}


@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class ConfigListener implements Listener<KafkaConfig> {
    private final List<Observer<KafkaConfig>> observerRegistry = new CopyOnWriteArrayList<>();
    private final Map<String, KafkaConfig> configRegistry = new HashMap<>();
    private final KafkaConfigService kafkaConfigService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final static String REDIS_KEY = "kafka_alarm_configs";

    @Override
    public void registerObserver(Observer<KafkaConfig> o) {
        observerRegistry.add(o);
        for (Map.Entry<String, KafkaConfig> entry: configRegistry.entrySet()) {
            o.onInit(entry.getValue());
        }
    }

    @Override
    public void removeObserver(Observer<KafkaConfig>  o) {
        observerRegistry.remove(o);
    }

    @Override
    public void notifyObservers(KafkaConfig config) {
        for (Observer<KafkaConfig> o : observerRegistry) {
            o.onUpdate(config);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void poll() {
        try {
            log.info("start check kafka connect config");
            // 1.从redis中获取配置
            Map<String , KafkaConfig> redisConfigs = trySyncConfigsFromRedis();
            // 2.从数据库中获取配置
            Map<String, KafkaConfig> databaseConfigs = trySyncConfigsFromPG();
            if (!redisConfigs.equals(databaseConfigs)) {
                // 两配置不相等需要同步
                log.info("Kafka configuration listener detected a configuration change and started synchronizing the configuration.");
                syncConfigToRedis(databaseConfigs);
            }
            if (!configRegistry.equals(databaseConfigs)) {
                // 将配置同步到本地
                syncConfigToLocal(databaseConfigs);
                // 通知观察者更新
                for (KafkaConfig config : configRegistry.values()) {
                    notifyObservers(config);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("parse kafka alarm config error {}", e.toString());
            throw new RuntimeException(e);
        }
    }

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


    private Map<String , KafkaConfig> trySyncConfigsFromPG() throws JsonProcessingException {
        HashMap<String , KafkaConfig> configs = new HashMap<>();
        for (KafkaConfig config : kafkaConfigService.list()) {
            configs.put(config.getName(), config);
        }
        return configs;
    }

    private void syncConfigToRedis(Map<String, KafkaConfig> configs) throws JsonProcessingException {
        stringRedisTemplate.delete(REDIS_KEY);
        for (Map.Entry<String, KafkaConfig> entry : configs.entrySet()) {
            String content = objectMapper.writeValueAsString(entry.getValue());
            stringRedisTemplate.opsForHash().put(REDIS_KEY, entry.getKey(), content);
        }
    }

    private void syncConfigToLocal(Map<String, KafkaConfig> configs) throws JsonProcessingException {
        configRegistry.clear();
        configRegistry.putAll(configs);
    }
}
