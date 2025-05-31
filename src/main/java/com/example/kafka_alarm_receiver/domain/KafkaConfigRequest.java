package com.example.kafka_alarm_receiver.domain;

import lombok.Data;

@Data
public class KafkaConfigRequest {
    private Long id;
    /**
     * 应用名称
     */
    private String name;
    /**
     * kafka地址
     */
    private String address;
    /**
     * 主题
     */
    private String topic;
    /**
     * 消费组
     */
    private String consumerGroup;
    /**
     * 认证机制
     */
    private String authentication;
    /**
     * 账户名称
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 创建用户id
     */
    private Long createUserId;
    /**
     * 更新用户id
     */
    private Long updateUserId;
    /**
     * kafka类型(0-采控，1-新城)
     */
    private Integer dataResource;
}
