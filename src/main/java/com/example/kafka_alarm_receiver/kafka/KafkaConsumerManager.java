package com.example.kafka_alarm_receiver.kafka;


import com.example.kafka_alarm_receiver.domain.AlarmMessage;
import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.es.ElasticsearchService;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("ALL")
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerManager implements ApplicationRunner {

    private final KafkaConfigService kafkaConfigService;
    private final ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory;
    private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers = new ConcurrentHashMap<>();
    private final ElasticsearchService elasticsearchService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void run(ApplicationArguments args) {
        log.info("🟢 [启动] 加载所有 Kafka 消费配置...");
        try {
            startAllListeners();
        } catch (Exception e) {
            log.error("❌ [错误] 初始化 Kafka 监听器失败", e);
        }
    }

    public void startAllListeners() {
        // 1. 获取所有kafka连接配置
        List<KafkaConfig> list = kafkaConfigService.list();
        int consumerCount = 0;
        for (KafkaConfig config : list) {
            // 2.检查连接状态
            if (config.getConnectionStatus() == 1) {
                this.addListener(config);
                consumerCount++;
            }
        }
        log.info("✅ [完成] 已加载 {} 个 Kafka 监听器", consumerCount);
    }

    public void addListener(KafkaConfig config) {
        String topic = config.getTopic();
        String application = config.getName();
        if (containers.containsKey(topic)) {
            log.warn("⚠️ [跳过] 已存在监听器，topic={}，跳过创建", topic);
            return;
        }

        try {
            ContainerProperties containerProps = new ContainerProperties(topic);
            containerProps.setGroupId(config.getConsumerGroup());

            containerProps.setMessageListener((MessageListener<String, String>) record -> {
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
            });

            ConcurrentMessageListenerContainer<String, String> container =
                    new ConcurrentMessageListenerContainer<>(createConsumerFactory(config), containerProps);

            container.start();
            containers.put(application, container);

            log.info("✅ [启动成功] Kafka 监听器已启动 | topic={} | group={}", topic, config.getConsumerGroup());
        } catch (Exception e) {
            log.error("🔥 [失败] 启动 Kafka 监听器失败 | application={} |topic={} | group={}", application, topic, config.getConsumerGroup(),e);
        }
    }

    public void removeListener(String application) {
        if (containers.containsKey(application)) {
            try {
                containers.get(application).stop();
                containers.remove(application);
                log.info("🛑 [停止] 已停止监听器：{}", application);
            } catch (Exception e) {
                log.error("💥 [异常] 停止监听器失败：" + application, e);
            }
        } else {
            log.warn("⚠️ [无效操作] 监听器 {} 不存在，无法停止", application);
        }
    }

    public void removeAllListeners() {
        log.info("🧹 [清理] 准备停止所有 Kafka 监听器...");
        for (Map.Entry<String, ConcurrentMessageListenerContainer<String, String>> entry : containers.entrySet()) {
            String name = entry.getKey();
            try {
                entry.getValue().stop();
                log.info("🛑 [已停止] 监听器 {}", name);
            } catch (Exception e) {
                log.error("🔥 [错误] 停止监听器 {} 失败", name, e);
            }
        }
        containers.clear();
        log.info("✅ [完成] 所有 Kafka 监听器已成功清理");
    }

    public void restartAllListeners() {
        removeAllListeners();
        startAllListeners();
    }

    private ConsumerFactory<String, String> createConsumerFactory(KafkaConfig config) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getAddress());
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, config.getAuthentication());

        String jaasTemplate = "org.apache.kafka.common.security.%s required username=\"%s\" password=\"%s\";";
        String loginModule = config.getAuthentication().equalsIgnoreCase("PLAIN")
                ? "plain.PlainLoginModule"
                : "scram.ScramLoginModule";
        String jaasConfig = String.format(jaasTemplate, loginModule, config.getUsername(), config.getPassword());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);

        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        return new DefaultKafkaConsumerFactory<>(props);
    }
}

