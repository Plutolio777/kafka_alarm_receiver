package com.example.kafka_alarm_receiver;

import com.example.kafka_alarm_receiver.domain.KafkaConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@MapperScan("com.example.kafka_alarm_receiver.mapper")
public class KafkaAlarmReceiverApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpringApplication.run(KafkaAlarmReceiverApplication.class, args);
    }

}
