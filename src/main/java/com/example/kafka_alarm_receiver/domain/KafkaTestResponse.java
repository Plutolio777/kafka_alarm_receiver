package com.example.kafka_alarm_receiver.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaTestResponse {
    private Long status;
    private String error;
}
