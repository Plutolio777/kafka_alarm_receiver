package com.example.kafka_alarm_receiver.service.impl;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import com.example.kafka_alarm_receiver.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * KafkaServiceImpl 是 Kafka 相关操作的业务实现类。
 * 提供创建 Kafka 管理配置、消费者配置以及测试 Kafka 连接的功能。
 */
@Slf4j
@Service
public class KafkaServiceImpl implements KafkaService {

    /**
     * 创建用于 Kafka AdminClient 的配置属性。
     *
     * @param config Kafka 配置对象，包含地址、认证方式、用户名和密码等信息
     * @return 返回配置好的 Properties 对象
     */
    public Properties createAdminConfig(KafkaConfig config) {
        Properties props = new Properties();
        // 设置 Kafka 服务器地址
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getAddress());
        // 使用 SASL_PLAINTEXT 安全协议
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        // 设置 SASL 认证机制（如 PLAIN 或 SCRAM）
        props.put(SaslConfigs.SASL_MECHANISM, config.getAuthentication());

        // 构建 JAAS 登录模块配置
        String jaasTemplate = "org.apache.kafka.common.security.%s required username=\"%s\" password=\"%s\";";
        String loginModule = config.getAuthentication().equalsIgnoreCase("PLAIN")
                ? "plain.PlainLoginModule"
                : "scram.ScramLoginModule";
        String jaasConfig = String.format(jaasTemplate, loginModule, config.getUsername(), config.getPassword());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);

        // 请求超时时间设置为 10 秒
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");

        return props;
    }

    /**
     * 创建 Kafka 消费者的配置属性。
     *
     * @param config Kafka 配置对象
     * @return 返回配置好的 Properties 对象，可用于创建 KafkaConsumer
     */
    public Properties createConsumerConfig(KafkaConfig config) {
        Properties props = this.createAdminConfig(config);
        // 消费者组 ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        // 自动提交偏移量策略：从最新开始消费
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // 键反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 值反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    /**
     * 测试是否能够成功连接到 Kafka 集群。
     *
     * @param config Kafka 配置对象
     * @return 如果连接成功返回 true，否则返回 false
     */
    @Override
    public boolean testConnection(KafkaConfig config) {
        Properties props = this.createAdminConfig(config);
        try (AdminClient adminClient = AdminClient.create(props)) {
            // 获取 Kafka 中的所有主题名称列表，用于测试连接
            ListTopicsResult topics = adminClient.listTopics();
            topics.names().get(); // 触发实际调用以验证连接
            return true;
        } catch (TimeoutException e) {
            log.error("Kafka connection timed out: ", e);
        } catch (Exception e) {
            log.error("Failed to connect to Kafka: ", e);
        }
        return false;
    }
}
