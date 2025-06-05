
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * StatisticsController 是一个 REST 控制器，用于处理与告警统计相关的 HTTP 请求。
 * 提供今日告警总数统计和按时间范围的告警趋势统计功能。
 */
@Slf4j
@RestController
@RequestMapping("/kafka/resource")
@RequiredArgsConstructor
public class StatisticsController {

    // 告警日志服务，用于从 Elasticsearch 获取告警数据
    private final ElasticAlarmLogService alarmLogService;

    /**
     * 获取今日的告警总数（分为 CK 和 XC 两类），并计算比例。
     *
     * @return 包含 CK/XC 告警数量及比例的响应实体
     * @throws IOException 如果发生 I/O 错误
     * @throws ElasticsearchException 如果 Elasticsearch 查询失败
     */
    @GetMapping("/count")
    public ResponseEntity<AlarmCountResponse> getTodayAlarmCount() throws IOException, ElasticsearchException {
        AlarmCountResponse.AlarmCountResponseBuilder builder = AlarmCountResponse.builder();
        long ckCount = alarmLogService.fetchCKAlarmCount();   // 获取 CK 类型告警数
        long xcCount = alarmLogService.fetchXCAlarmCount();   // 获取 XC 类型告警数

        builder.ckCount(ckCount);
        builder.xcCount(xcCount);

        // 计算比例：CK 占比固定为 1，XC 占比根据 CK 数量动态计算
        builder.ckPercentage("1");
        if (ckCount != 0L) {
            Double xcPercentage = (double) xcCount / ckCount;
            builder.xcPercentage(String.format("%.2f", xcPercentage));
        } else {
            builder.ckPercentage("0.00");
        }

        return ResponseEntity.ok(builder.build());
    }

    /**
     * 根据指定的时间范围和类型获取告警的时间分布统计数据。
     *
     * @param startDate 起始时间（可选）
     * @param endDate 结束时间（可选）
     * @param type 查询类型（例如：小时、天等）
     * @return 返回按时间分组的告警数量统计结果
     * @throws IOException 如果发生 I/O 错误
     * @throws ParseException 如果日期格式解析失败
     */
    @GetMapping("/select")
    public List<AlarmTimeCount> getAlarmStatistics(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
            @RequestParam("type") Integer type) throws IOException, ParseException {

        ArrayList<AlarmTimeCount> list = new ArrayList<>();

        // resourceType: 0=CK, 1=XC
        for (int resourceType = 0; resourceType <= 1; resourceType++) {
            List<ElasticAlarmLogServiceImpl.DateHistogramResult> dateHistogramResults =
                    alarmLogService.fetchAlarmDateHistogram(type, resourceType, startDate, endDate);

            for (var result : dateHistogramResults) {
                // 查找是否已有该时间点的数据
                AlarmTimeCount existing = list.stream()
                        .filter(r -> r.getCurrTime().equals(result.formatTime()))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    // 创建新的时间点记录
                    AlarmTimeCount newCount = AlarmTimeCount.builder()
                            .currTime(result.formatTime())
                            .ckCount(resourceType == 0 ? result.count() : 0)
                            .xcCount(resourceType == 1 ? result.count() : 0)
                            .build();
                    list.add(newCount);
                } else {
                    // 更新已有记录中的 CK/XC 告警数
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