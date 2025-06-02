package com.example.kafka_alarm_receiver.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KafkaConfigRequest {


    private Long id;
    @NotNull(message = "应用名称不能为空")
    @NotBlank(message = "应用名称不能为空")
    private String name;
    @NotNull(message = "Kafka连接地址不能为空")
    @NotBlank(message = "Kafka连接地址不能为空")
    private String address;
    @NotNull(message = "Kafka Topic不能为空")
    @NotBlank(message = "Kafka Topic不能为空")
    private String topic;

    @NotNull(message = "Kafka 消费者组不能为空")
    @NotBlank(message = "Kafka 消费者组不能为空")
    private String consumerGroup;

    private String authentication;
    private String username;
    private String password;

    private Long createUserId;
    private Long updateUserId;
    private Integer dataResource;
}
