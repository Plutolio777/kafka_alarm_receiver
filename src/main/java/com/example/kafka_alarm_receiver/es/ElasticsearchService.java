package com.example.kafka_alarm_receiver.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.kafka_alarm_receiver.domain.AlarmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchClient client;

    public void saveAlarm(AlarmMessage alarmMessage) {
//        String indexName = "kafka_alarm_log-" +
//                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//
//        IndexRequest<AlarmMessage> request = IndexRequest.of(i -> i
//                .index(indexName)  // 使用生成的索引名
//                .document(alarmMessage)
//        );
        IndexRequest<AlarmMessage> request = IndexRequest.of(i -> i
                .index("<kafka_alarm_log-{now/d}>")  // 你的索引名
                .document(alarmMessage)
        );
        try {
            IndexResponse response = client.index(request);
            log.info("Indexed with id: " + response.id());
        } catch (ElasticsearchException | IOException e) {
            log.error("Elasticsearch error: " ,e);
        }
    }
}
