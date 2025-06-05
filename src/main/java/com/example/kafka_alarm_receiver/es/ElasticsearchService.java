package com.example.kafka_alarm_receiver.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.kafka_alarm_receiver.domain.AlarmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * ElasticsearchService 是一个 Spring 服务类，用于将告警信息保存到 Elasticsearch。
 * 使用 Elasticsearch 高级 Java 客户端进行索引操作。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchService {

    // 注入的 Elasticsearch 客户端实例
    private final ElasticsearchClient client;

    /**
     * 将告警信息写入 Elasticsearch 索引。
     *
     * @param alarmMessage 要写入的告警消息对象
     */
    public void saveAlarm(AlarmMessage alarmMessage) {
        // 构建索引请求，指定索引名和文档内容
        IndexRequest<AlarmMessage> request = IndexRequest.of(i -> i
                .index("<kafka_alarm_log-{now/d}>")  // 按天动态创建索引，例如 kafka_alarm_log-2023-10-05
                .document(alarmMessage)
        );

        try {
            // 执行索引操作并获取响应
            IndexResponse response = client.index(request);
            log.info("Indexed with id: " + response.id());
        } catch (ElasticsearchException | IOException e) {
            // 捕获并记录 Elasticsearch 异常或 IO 异常
            log.error("Elasticsearch error: ", e);
        }
    }
}
