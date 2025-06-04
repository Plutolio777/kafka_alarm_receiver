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

@Slf4j
@Service
public class KafkaServiceImpl implements KafkaService {



    public Properties createAdminConfig(KafkaConfig config) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getAddress());
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, config.getAuthentication());
        String jaasTemplate = "org.apache.kafka.common.security.%s required username=\"%s\" password=\"%s\";";
        String loginModule = config.getAuthentication().equalsIgnoreCase("PLAIN")
                ? "plain.PlainLoginModule"
                : "scram.ScramLoginModule";
        String jaasConfig = String.format(jaasTemplate, loginModule, config.getUsername(), config.getPassword());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        return props;
    }


    public Properties createConsumerConfig(KafkaConfig config) {
        Properties props = this.createAdminConfig(config);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Override
    public boolean testConnection(KafkaConfig config) {
        Properties props = this.createAdminConfig(config);
        try (AdminClient adminClient = AdminClient.create(props)) {
            ListTopicsResult topics = adminClient.listTopics();
            topics.names().get();// 获取所有主题名称
            return true;
        } catch (TimeoutException e) {
            log.error("Kafka connection timed out: " + e);
        } catch (Exception e) {
            log.error("Failed to connect to Kafka: " + e);
        }
        return false;
    }
}
