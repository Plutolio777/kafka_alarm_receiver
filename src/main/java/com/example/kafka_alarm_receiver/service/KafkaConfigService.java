package com.example.kafka_alarm_receiver.service;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Dell
* @description 针对表【kafka_config】的数据库操作Service
* @createDate 2025-05-31 22:39:46
*/
public interface KafkaConfigService extends IService<KafkaConfig> {

    KafkaConfig getConfigByName(String name);
    void markSuccess(String name);

    void markFail(String name);
}
