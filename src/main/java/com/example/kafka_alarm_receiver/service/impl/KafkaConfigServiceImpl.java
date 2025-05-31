package com.example.kafka_alarm_receiver.service.impl;

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

}




