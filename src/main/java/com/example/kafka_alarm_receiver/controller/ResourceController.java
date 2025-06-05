
package com.example.kafka_alarm_receiver.controller;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.domain.KafkaConfigRequest;
import com.example.kafka_alarm_receiver.domain.KafkaConfigResponse;
import com.example.kafka_alarm_receiver.domain.KafkaTestResponse;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.example.kafka_alarm_receiver.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ResourceController 是一个 REST 控制器，负责处理 Kafka 配置相关的 HTTP 请求。
 * 包括配置的收集、获取和测试功能。
 */
@SuppressWarnings("ALL")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka/resource")
public class ResourceController {

    // Kafka 配置服务，用于操作 Kafka 配置数据
    private final KafkaConfigService kafkaConfigService;

    // Kafka 服务，用于测试 Kafka 连接等操作
    private final KafkaService kafkaService;

    /**
     * 接收并处理 Kafka 配置信息的批量更新请求。
     * 先清空数据库中所有旧配置，然后保存新配置。
     *
     * @param resources 新的 Kafka 配置列表
     * @return 响应结果
     */
    @PostMapping("/collect")
    public ResponseEntity<KafkaConfigResponse> collectResources(@RequestBody List<KafkaConfigRequest> resources) {
        log.info("receive update collect config requests {}", resources);
        // 清空数据库中的所有记录
        kafkaConfigService.remove(null);
        for (KafkaConfigRequest request : resources) {
            KafkaConfig config = new KafkaConfig();
            mapRequestToEntity(request, config);
            config.setConnectionStatus(0); // 设置连接状态为 0（未连接）
            kafkaConfigService.save(config);
        }
        return ResponseEntity.ok(KafkaConfigResponse.builder().status(200L).build());
    }

    /**
     * 获取当前所有的 Kafka 配置，并为每条配置分配自增 ID。
     *
     * @return 返回 Kafka 配置列表
     */
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

    /**
     * 测试指定名称的 Kafka 配置是否可以成功连接。
     *
     * @param request 请求参数包含要测试的 Kafka 配置名称
     * @return 测试结果
     */
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
            return ResponseEntity.ok(builder.status(200L).error("").build());
        }
        builder.status(500L);
        builder.error("Connect error.");
        return ResponseEntity.status(500).body(builder.build());
    }

    /**
     * 将请求对象 KafkaConfigRequest 映射为实体对象 KafkaConfig。
     */
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