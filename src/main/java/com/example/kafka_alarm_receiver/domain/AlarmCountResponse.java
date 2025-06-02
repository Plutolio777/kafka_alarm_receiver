package com.example.kafka_alarm_receiver.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlarmCountResponse {
    private Long ckCount;
    // 新城告警数量
    private Long xcCount;
    // 采控百分比
    private String ckPercentage;
    // 新城百分比
    private String xcPercentage;
}
