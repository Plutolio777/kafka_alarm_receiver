package com.example.kafka_alarm_receiver.controller;

import com.example.kafka_alarm_receiver.domain.KafkaConfigRequest;
import com.example.kafka_alarm_receiver.domain.KafkaConfigResponse;
import com.example.kafka_alarm_receiver.mapper.KafkaConfigMapper;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.example.kafka_alarm_receiver.service.KafkaConsumerManager;
import com.example.kafka_alarm_receiver.service.impl.KafkaConfigServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kafka/resource")
public class ResourceController {


    private KafkaConfigService kafkaServer;
    private KafkaConsumerManager kafkaConsumerManager;


    @PostMapping("/collect")
    public ResponseEntity<KafkaConfigResponse> collectResources(@RequestBody List<KafkaConfigRequest> resources) {

        return ResponseEntity.status(200).body(new KafkaConfigResponse());

    }
}
