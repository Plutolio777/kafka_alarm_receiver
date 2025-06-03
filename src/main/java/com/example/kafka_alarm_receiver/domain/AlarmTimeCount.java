package com.example.kafka_alarm_receiver.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlarmTimeCount {
    private Long ckCount;
    private Long xcCount;
    private String currTime;
}
