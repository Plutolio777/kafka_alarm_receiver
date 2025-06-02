package com.example.kafka_alarm_receiver.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch.core.CountResponse;
import com.example.kafka_alarm_receiver.domain.AlarmCountResponse;
import com.example.kafka_alarm_receiver.domain.AlarmTimeCount;
import com.example.kafka_alarm_receiver.domain.KafkaTestResponse;
import com.example.kafka_alarm_receiver.service.StatisticsService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/kafka/resource")
@RequiredArgsConstructor
public class StatisticsController {


    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
@GetMapping("/select")
public List<AlarmTimeCount> getAlarmStatistics(
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
        @RequestParam("type") Integer type) {

    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now();

        CalendarInterval interval;
        String startDateStr;
        String endDateStr;

        switch (type) {
            case 1: // 近30天，按天
                endDateStr = sdf.format(Date.from(now.atZone(zoneId).toInstant()));
                startDateStr = sdf.format(Date.from(now.minusDays(30).atZone(zoneId).toInstant()));
                interval = CalendarInterval.Day;
                break;

            case 2: // 近一年，按月
                endDateStr = sdf.format(Date.from(now.atZone(zoneId).toInstant()));
                startDateStr = sdf.format(Date.from(now.minusYears(1).atZone(zoneId).toInstant()));
                interval = CalendarInterval.Month;
                break;

            case 3: // 自定义
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("自定义时间段需提供 startDate 和 endDate");
                }
                long diff = endDate.getTime() - startDate.getTime();
                interval = diff > 180L * 24 * 60 * 60 * 1000 ? CalendarInterval.Month : CalendarInterval.Day;
                startDateStr = sdf.format(startDate);
                endDateStr = sdf.format(endDate);
                break;

            case 4: // 当天，按小时
                LocalDateTime startOfDay = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
                startDateStr = sdf.format(Date.from(startOfDay.atZone(zoneId).toInstant()));
                endDateStr = sdf.format(Date.from(now.atZone(zoneId).toInstant()));
                interval = CalendarInterval.Hour;
                break;

            default:
                throw new IllegalArgumentException("不支持的type类型");
        }

        List<AlarmTimeCount> result = new java.util.ArrayList<>();
        for (long resourceType = 0; resourceType <= 1; resourceType++) {
            long finalResourceType = resourceType;
            var searchResponse = client.search(s -> s
                            .index("kafka_alarm_log-*")
                            .size(0)
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.range(r -> r.date(dt -> dt.field("SRC_EVENTTIME").gte(startDateStr).lt(endDateStr))))
                                    .must(m -> m.term(t -> t.field("DATA_RESOURCE").value(v -> v.longValue(finalResourceType))))))
                            .aggregations("timeAgg", a -> a
                                    .dateHistogram(h -> h
                                            .field("SRC_EVENTTIME")
                                            .calendarInterval(interval)
                                            .format("yyyy-MM-dd HH:mm"))),
                    Map.class);

            var buckets = searchResponse.aggregations()
                    .get("timeAgg")
                    .dateHistogram()
                    .buckets()
                    .array();

            for (var bucket : buckets) {
                String time = bucket.keyAsString();
                Long count = bucket.docCount();

                AlarmTimeCount existing = result.stream()
                        .filter(r -> r.getCurrTime().equals(time))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    AlarmTimeCount newCount = AlarmTimeCount.builder()
                            .currTime(time)
                            .ckCount(resourceType == 0 ? count : 0)
                            .cxCount(resourceType == 1 ? count : 0)
                            .build();
                    result.add(newCount);
                } else {
                    if (resourceType == 0) {
                        existing.setCkCount(count);
                    } else {
                        existing.setCxCount(count);
                    }
                }
            }
        }

        result.sort(java.util.Comparator.comparing(AlarmTimeCount::getCurrTime));
        return result;

    } catch (Exception e) {
        log.error("统计失败", e);
        return java.util.Collections.emptyList();
    }
}


}
