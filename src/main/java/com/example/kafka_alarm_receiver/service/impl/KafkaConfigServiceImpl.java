package com.example.kafka_alarm_receiver.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import com.example.kafka_alarm_receiver.mapper.KafkaConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author Dell
* @description 针对表【kafka_config】的数据库操作Service实现
* @createDate 2025-05-31 22:39:46
*/
@Service
public class KafkaConfigServiceImpl extends ServiceImpl<KafkaConfigMapper, KafkaConfig>
    implements KafkaConfigService{

    @Override
    public KafkaConfig getConfigByName(String name) {
        QueryChainWrapper<KafkaConfig> wrapper = this.query();
        wrapper.eq("name", name);
        return this.getOne(wrapper);
    }

    @Override
    public void markSuccess(String name) {
        UpdateChainWrapper<KafkaConfig> wrapper = this.update();
        wrapper.eq("name", name).set("connection_status", 1);
        wrapper.update();
    }

    @Override
    public void markFail(String name) {
        UpdateChainWrapper<KafkaConfig> wrapper = this.update();
        wrapper.eq("name", name).set("connection_status", 0);
        wrapper.update();
    }
}




