package com.example.kafka_alarm_receiver.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountResponse;
import com.example.kafka_alarm_receiver.domain.AlarmCountResponse;
import com.example.kafka_alarm_receiver.domain.KafkaTestResponse;
import com.example.kafka_alarm_receiver.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/kafka/resource")
@RequiredArgsConstructor
public class StatisticsController {

    private final ElasticsearchClient client;

    @GetMapping("/count")
    public ResponseEntity<AlarmCountResponse> getTodayAlarmCount(){
        CountResponse ckCountResponse = null;
        try {
                    AlarmCountResponse.AlarmCountResponseBuilder builder = AlarmCountResponse.builder();
            // 1.查询采控告警数
            ckCountResponse = client.count(c -> c
                    .index("kafka_alarm_log-*")
                    .query(q -> q.term(t -> t.field("DATA_RESOURCE").value(v -> v.longValue(0)))));
            long ckCount = ckCountResponse.count();
            builder.ckCount(ckCount);
            // 2.查询新城告警数
            ckCountResponse = client.count(c -> c
                    .index("kafka_alarm_log-*")
                    .query(q -> q.term(t -> t.field("DATA_RESOURCE").value(v -> v.longValue(1)))));
            long xcCount = ckCountResponse.count();
            builder.xcCount(xcCount);
            // 3.计算  系统a告警数量/系统b告警数量
            if (xcCount != 0L) {
                Double ckPercentage = (double) ckCount / xcCount;
                builder.ckPercentage(String.format("%.2f", ckPercentage));
            }else {
                builder.ckPercentage("0.00");
            }

            // 4.计算  系统b告警数量/系统a告警数量
            if (ckCount != 0L) {
                Double xcPercentage = (double) xcCount/ ckCount;
                builder.xcPercentage(String.format("%.2f", xcPercentage));
            }else {
                builder.ckPercentage("0.00");
            }
            return ResponseEntity.ok(builder.build());
        } catch (IOException e) {
            log.error("elasticsearch connect error: ", e);
            return ResponseEntity.status(500).body(AlarmCountResponse.builder().build());
        }
    }
//
//    @PostMapping("/select")
//    public List<AlarmCountVO> getAlarmStatistics(
//            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
//            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
//            @RequestParam("type") Integer type) {
//        return statisticsService.getAlarmStatistics(startDate, endDate, type);
//    }
}
