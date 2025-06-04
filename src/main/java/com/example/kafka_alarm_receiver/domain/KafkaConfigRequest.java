package com.example.kafka_alarm_receiver.domain;

import com.example.kafka_alarm_receiver.validation.EnumValue;
import com.example.kafka_alarm_receiver.validation.kafka.SaslAuthentication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KafkaConfigRequest {
    private Long id;
    @NotBlank(message = "应用名称不能为空")
    private String name;
    @NotBlank(message = "Kafka连接地址不能为空")
    private String address;
    @NotBlank(message = "Kafka Topic不能为空")
    private String topic;
    @NotBlank(message = "Kafka 消费者组不能为空")
    private String consumerGroup;
    @EnumValue(enumClass = SaslAuthentication.class, message = "只可选择PLAIN或SCRAM")
    private String authentication;
    @NotBlank(message = "Kafka 认证用户名不能为空")
    private String username;
    @NotBlank(message = "Kafka 认证密码不能为空")
    private String password;
    private Long createUserId;
    private Long updateUserId;
    @NotNull(message = "Kafka 数据源不能为空")
    private Integer dataResource;
}
