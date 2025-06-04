package com.example.kafka_alarm_receiver.validation.kafka;

public enum KafkaConnectionStatus {
    ACTIVE(1),
    INACTIVE(0);

    private final int code;

    KafkaConnectionStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static KafkaConnectionStatus fromCode(int code) {
        for (KafkaConnectionStatus status : KafkaConnectionStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
