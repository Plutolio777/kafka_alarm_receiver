package com.example.kafka_alarm_receiver.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonErrorResponse {
    long status;
    String message;
}
