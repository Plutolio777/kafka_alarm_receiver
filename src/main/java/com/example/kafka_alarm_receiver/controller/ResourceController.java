package com.example.kafka_alarm_receiver.controller;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.domain.KafkaConfigRequest;
import com.example.kafka_alarm_receiver.domain.KafkaConfigResponse;
import com.example.kafka_alarm_receiver.domain.KafkaTestResponse;
import com.example.kafka_alarm_receiver.kafka.KafkaConsumerManager;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.example.kafka_alarm_receiver.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka/resource")
public class ResourceController {

    private final KafkaConfigService kafkaConfigService;

    private final KafkaConsumerManager consumerManager;

    private final KafkaService kafkaService;

    @PostMapping("/collect")
    public ResponseEntity<KafkaConfigResponse> collectResources(@RequestBody List<KafkaConfigRequest> resources) {
        log.info("receive update collect config requests {}", resources);
        // 清空数据表中所有记录
        kafkaConfigService.remove(null);
        for (KafkaConfigRequest request : resources) {
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
    public ResponseEntity<KafkaTestResponse> testResource(@RequestBody KafkaConfigRequest request) {
        KafkaTestResponse.KafkaTestResponseBuilder builder = KafkaTestResponse.builder();
        KafkaConfig config = kafkaConfigService.getConfigByName(request.getName());
        if (config == null) {
            builder.status(500L);
            builder.error("Kafka configuration not found for the provided name.");
            return ResponseEntity.status(500).body(builder.build());
        }
        if (kafkaService.testConnection(config)) {
            kafkaConfigService.markSuccess(request.getName());
            consumerManager.restartAllListeners();
            return ResponseEntity.ok(builder.status(200L).error("").build());
        }
        builder.status(500L);
        builder.error("Connect error.");
        return ResponseEntity.status(500).body(builder.build());
    }


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
