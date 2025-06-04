package com.example.kafka_alarm_receiver.controller;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.example.kafka_alarm_receiver.domain.AlarmCountResponse;
import com.example.kafka_alarm_receiver.domain.AlarmTimeCount;
import com.example.kafka_alarm_receiver.service.ElasticAlarmLogService;
import com.example.kafka_alarm_receiver.service.impl.ElasticAlarmLogServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/kafka/resource")
@RequiredArgsConstructor
public class StatisticsController {

    private final ElasticAlarmLogService alarmLogService;

    @GetMapping("/count")
    public ResponseEntity<AlarmCountResponse> getTodayAlarmCount() throws IOException, ElasticsearchException {
        AlarmCountResponse.AlarmCountResponseBuilder builder = AlarmCountResponse.builder();
        long ckCount = alarmLogService.fetchCKAlarmCount();
        long xcCount = alarmLogService.fetchXCAlarmCount();
        builder.ckCount(ckCount);
        builder.xcCount(xcCount);
        // 3.计算  系统a告警数量/系统b告警数量
        builder.ckPercentage("1");
        // 4.计算  系统b告警数量/系统a告警数量
        if (ckCount != 0L) {
            Double xcPercentage = (double) xcCount/ ckCount;
            builder.xcPercentage(String.format("%.2f", xcPercentage));
        }else {
            builder.ckPercentage("0.00");
        }
        return ResponseEntity.ok(builder.build());
    }


    @GetMapping("/select")
    public List<AlarmTimeCount> getAlarmStatistics(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
            @RequestParam("type") Integer type) throws IOException, ParseException {

        ArrayList<AlarmTimeCount> list = new ArrayList<>();
        for (int resourceType = 0; resourceType <= 1; resourceType++) {
            List<ElasticAlarmLogServiceImpl.DateHistogramResult> dateHistogramResults = alarmLogService.fetchAlarmDateHistogram(type, resourceType, startDate, endDate);
            for (var result : dateHistogramResults) {
                AlarmTimeCount existing = list.stream()
                        .filter(r -> r.getCurrTime().equals(result.formatTime()))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    AlarmTimeCount newCount = AlarmTimeCount.builder()
                            .currTime(result.formatTime())
                            .ckCount(resourceType == 0 ? result.count() : 0)
                            .xcCount(resourceType == 1 ? result.count() : 0)
                            .build();
                    list.add(newCount);
                } else {
                    if (resourceType == 0) {
                        existing.setCkCount(result.count());
                    } else {
                        existing.setXcCount(result.count());
                    }
                }
            }
        }
        return list;
    }
}




