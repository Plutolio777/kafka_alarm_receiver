package com.example.kafka_alarm_receiver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.kafka_alarm_receiver.domain.KafkaConfig;

/**
 * KafkaConfigService 是一个针对数据库表【kafka_config】的业务操作接口。
 * 提供基础的 CRUD 操作以及根据配置名称查询和更新连接状态的功能。
 *
 * @author Dell
 * @createDate 2025-05-31 22:39:46
 */
public interface KafkaConfigService extends IService<KafkaConfig> {

    /**
     * 根据配置名称查询 Kafka 配置信息。
     *
     * @param name 配置名称
     * @return 返回对应的 KafkaConfig 对象，如果不存在则返回 null
     */
    KafkaConfig getConfigByName(String name);

    /**
     * 标记指定名称的 Kafka 配置为连接成功状态（connection_status = 1）。
     *
     * @param name 配置名称
     */
    void markSuccess(String name);

    /**
     * 标记指定名称的 Kafka 配置为连接失败状态（connection_status = 0）。
     *
     * @param name 配置名称
     */
    void markFail(String name);
}
