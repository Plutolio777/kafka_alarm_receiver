package com.example.kafka_alarm_receiver.service;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;

import java.util.Properties;

/**
 * KafkaService 是一个服务接口，定义了与 Kafka 相关的核心操作。
 * 提供创建 Kafka 配置（管理端和消费端）以及测试 Kafka 连接的功能。
 */
public interface KafkaService {

    /**
     * 测试是否能够成功连接到 Kafka 集群。
     *
     * @param config Kafka 配置信息
     * @return 如果连接成功返回 true，否则返回 false
     */
    boolean testConnection(KafkaConfig config);

    /**
     * 创建 Kafka AdminClient 所需的配置属性。
     *
     * @param config Kafka 配置信息，用于构建连接属性
     * @return 返回配置好的 Properties 对象，可用于创建 AdminClient
     */
    Properties createAdminConfig(KafkaConfig config);

    /**
     * 创建 Kafka Consumer 所需的配置属性。
     *
     * @param config Kafka 配置信息
     * @return 返回配置好的 Properties 对象，可用于创建 KafkaConsumer
     */
    Properties createConsumerConfig(KafkaConfig config);
}
