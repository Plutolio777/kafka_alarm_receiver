package com.example.kafka_alarm_receiver.validation.kafka;

/**
 * KafkaConnectionStatus 枚举类用于表示 Kafka 配置的连接状态。
 * 支持将整型状态码与枚举值相互转换。
 */
public enum KafkaConnectionStatus {
    /**
     * 表示 Kafka 连接处于活跃状态（code = 1）
     */
    ACTIVE(1),

    /**
     * 表示 Kafka 连接处于非活跃状态（code = 0）
     */
    INACTIVE(0);

    private final int code;

    /**
     * 构造方法，用于绑定每个枚举值对应的状态码
     *
     * @param code 状态码
     */
    KafkaConnectionStatus(int code) {
        this.code = code;
    }

    /**
     * 获取该枚举值对应的状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 根据状态码获取对应的枚举值
     *
     * @param code 状态码
     * @return 对应的 KafkaConnectionStatus 枚举
     * @throws IllegalArgumentException 如果传入的状态码没有对应的枚举值
     */
    public static KafkaConnectionStatus fromCode(int code) {
        for (KafkaConnectionStatus status : KafkaConnectionStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
