package com.example.kafka_alarm_receiver.service;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;

import java.util.Properties;

public interface KafkaService {
    boolean testConnection(KafkaConfig config);
    Properties createAdminConfig(KafkaConfig config);
    Properties createConsumerConfig(KafkaConfig config);
}
