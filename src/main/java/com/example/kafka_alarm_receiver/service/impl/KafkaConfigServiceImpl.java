package com.example.kafka_alarm_receiver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.mapper.KafkaConfigMapper;
import com.example.kafka_alarm_receiver.service.KafkaConfigService;
import org.springframework.stereotype.Service;

/**
 * KafkaConfigServiceImpl 是 Kafka 配置的业务实现类，用于对数据库表【kafka_config】进行操作。
 * 提供根据名称查询配置、标记连接成功或失败等方法。
 *
 * @author Dell
 * @createDate 2025-05-31 22:39:46
 */
@Service
public class KafkaConfigServiceImpl extends ServiceImpl<KafkaConfigMapper, KafkaConfig>
        implements KafkaConfigService {

        /**
         * 根据配置名称查询 Kafka 配置信息。
         *
         * @param name 配置名称
         * @return 返回对应的 KafkaConfig 对象，如果不存在则返回 null
         */
        @Override
        public KafkaConfig getConfigByName(String name) {
            LambdaQueryWrapper<KafkaConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KafkaConfig::getName, name);
            return this.getOne(wrapper);
        }

    /**
     * 标记指定名称的 Kafka 配置为连接成功状态（connection_status = 1）。
     *
     * @param name 配置名称
     */
    @Override
    public void markSuccess(String name) {
        UpdateChainWrapper<KafkaConfig> wrapper = this.update();
        wrapper.eq("name", name).set("connection_status", 1); // 构造更新语句：SET connection_status = 1 WHERE name = ?
        wrapper.update(); // 执行更新
    }

    /**
     * 标记指定名称的 Kafka 配置为连接失败状态（connection_status = 0）。
     *
     * @param name 配置名称
     */
    @Override
    public void markFail(String name) {
        UpdateChainWrapper<KafkaConfig> wrapper = this.update();
        wrapper.eq("name", name).set("connection_status", 0); // 构造更新语句：SET connection_status = 0 WHERE name = ?
        wrapper.update(); // 执行更新
    }
}
