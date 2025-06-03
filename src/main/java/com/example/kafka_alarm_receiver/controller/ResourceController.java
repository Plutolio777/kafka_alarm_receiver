package com.example.kafka_alarm_receiver.controller;

import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.example.kafka_alarm_receiver.domain.*;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.example.kafka_alarm_receiver.kafka.KafkaConsumerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka/resource")
public class ResourceController {

    private final KafkaConfigService kafkaConfigService;

    private final KafkaConsumerManager consumerManager;

    @PostMapping("/collect")
    public ResponseEntity<KafkaConfigResponse> collectResources(@RequestBody List<KafkaConfigRequest> resources) {
        log.info("receive update collect config requests {}", resources);
        // 清空数据表中所有记录
        kafkaConfigService.remove(null);
        for (KafkaConfigRequest request : resources) {
            // 1.认证参数校验
            String authentication = request.getAuthentication();
            if (!"".equals(authentication) && authentication != null) {
                if (!"PLAIN".equals(authentication) && !"SCRAM".equals(authentication) ) {
                    return ResponseEntity.ok(
                            KafkaConfigResponse.builder()
                                    .status(500L)
                                    .error("only support PLAIN and SCRAM authentication with username and passowrd")
                                    .build());
                }

            }
            KafkaConfig config = new KafkaConfig();
            mapRequestToEntity(request, config);
            config.setConnectionStatus(0); // 设置连接状态为0
            kafkaConfigService.save(config);
        }

        return ResponseEntity.ok(KafkaConfigResponse.builder().status(200L).build());
    }

    @GetMapping("/collect")
    public ResponseEntity<List<KafkaConfig>> fetchResource() {
        AtomicInteger id = new AtomicInteger(1);
        List<KafkaConfig> list = kafkaConfigService.list();

        list = list.stream().peek(kafkaConfig -> {
            kafkaConfig.setId(id.get());
            id.getAndIncrement();
        }).toList();
        return ResponseEntity.ok(list);
    }


    @PostMapping("/test")
    public ResponseEntity<KafkaTestResponse> testResource(@RequestBody KafkaConfigRequest config) {

        KafkaTestResponse.KafkaTestResponseBuilder builder = KafkaTestResponse.builder();
        Properties props = new Properties();
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
        try (AdminClient adminClient = AdminClient.create(props)) {
            // 列出 Kafka 主题
            ListTopicsResult topics = adminClient.listTopics();
            topics.names().get();// 获取所有主题名称
            log.info("Kafka connectivity test successful!");
            builder.status(200L);
            UpdateChainWrapper<KafkaConfig> wrapper = kafkaConfigService.update();
            wrapper.eq("name", config.getName()).set("connection_status", 1);
            wrapper.update();
            // 重启消费者
            consumerManager.restartAllListeners();
        } catch (TimeoutException e) {
            log.error("Kafka connection timed out: " + e);
            builder.status(500L);
            builder.error("kafka connection timeout");
        } catch (Exception e) {
            log.error("Failed to connect to Kafka: " + e);
            builder.status(500L);
            builder.error("Failed to connect to Kafka");
        }
        return ResponseEntity.ok(builder.build());
    }

    // 将请求参数映射到实体对象
    private void mapRequestToEntity(KafkaConfigRequest request, KafkaConfig entity) {
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setTopic(request.getTopic());
        entity.setConsumerGroup(request.getConsumerGroup());
        entity.setAuthentication(request.getAuthentication());
        entity.setUsername(request.getUsername());
        entity.setPassword(request.getPassword());
        entity.setCreateUserId(request.getCreateUserId());
        entity.setUpdateUserId(request.getUpdateUserId());
        entity.setDataResource(request.getDataResource());
    }
}
