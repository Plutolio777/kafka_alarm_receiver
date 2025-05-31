package com.example.kafka_alarm_receiver.service;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class KafkaConsumerManager implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(1111);
    }

//    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerManager.class);
//
//    private final ResourceRepository resourceRepository;
//    private final EsService esService;
//    private final ExecutorService executor = Executors.newCachedThreadPool();
//    private final Map<String, Future<?>> consumerTasks = new ConcurrentHashMap<>();
//    private final Map<String, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();
//
//    @Autowired
//    public KafkaConsumerManager(ResourceRepository resourceRepository, EsService esService) {
//        this.resourceRepository = resourceRepository;
//        this.esService = esService;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        startAllConsumers();
//    }
//
//    public void startAllConsumers() {
//        List<ResourceDO> configs = resourceRepository.findAll();
//        for (ResourceDO config : configs) {
//            startConsumer(config);
//        }
//    }
//
//    public void stopAllConsumers() {
//        for (Map.Entry<String, Future<?>> entry : consumerTasks.entrySet()) {
//            stopConsumer(entry.getKey());
//        }
//    }
//
//    public void restartAllConsumers() {
//        stopAllConsumers();
//        startAllConsumers();
//    }
//
//    public void startConsumer(ResourceDO config) {
//        String appName = config.getName();
//        if (runningFlags.containsKey(appName) {
//            logger.warn("消费者 {} 已经在运行", appName);
//            return;
//        }
//
//        AtomicBoolean running = new AtomicBoolean(true);
//        runningFlags.put(appName, running);
//
//        Future<?> future = executor.submit(() -> {
//            Properties props = new Properties();
//            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getAddress());
//            props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
//            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//
//            // 配置SASL认证
//            if (config.getAuthentication() != null) {
//                props.put("security.protocol", "SASL_PLAINTEXT");
//                props.put("sasl.mechanism", "PLAIN");
//                props.put("sasl.jaas.config",
//                    "org.apache.kafka.common.security.plain.PlainLoginModule required " +
//                    "username=\"" + config.getUsername() + "\" " +
//                    "password=\"" + config.getPassword() + "\";");
//            }
//
//            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
//                consumer.subscribe(Collections.singletonList(config.getTopic()));
//
//                while (running.get()) {
//                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//                    for (ConsumerRecord<String, String> record : records) {
//                        esService.saveAlarm(record.value(), config.getDataResource());
//                    }
//                    consumer.commitSync();
//                }
//            } catch (Exception e) {
//                logger.error("消费者 {} 运行异常", appName, e);
//            } finally {
//                runningFlags.remove(appName);
//                logger.info("消费者 {} 已停止", appName);
//            }
//        });
//
//        consumerTasks.put(appName, future);
//        logger.info("消费者 {} 已启动", appName);
//    }
//
//    public void stopConsumer(String appName) {
//        AtomicBoolean running = runningFlags.get(appName);
//        if (running != null) {
//            running.set(false);
//        }
//
//        Future<?> future = consumerTasks.get(appName);
//        if (future != null) {
//            future.cancel(true);
//            consumerTasks.remove(appName);
//        }
//    }
}
