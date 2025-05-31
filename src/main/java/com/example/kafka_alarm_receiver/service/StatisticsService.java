package com.example.kafka_alarm_receiver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {
//
//    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
//    private static final DateTimeFormatter INDEX_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
//
//    private final ElasticsearchClient esClient;
//
//    @Autowired
//    public StatisticsService(ElasticsearchClient esClient) {
//        this.esClient = esClient;
//    }
//
//    public AlarmCountVO getTodayAlarmCount() {
//        try {
//            // 获取当日索引名称
//            String todayIndex = "kafka_alarm_log-" + LocalDate.now().format(INDEX_FORMATTER);
//
//            // 构建按dataResource分组的聚合
//            Map<String, Aggregation> aggregations = new HashMap<>();
//            aggregations.put("by_resource", Aggregation.of(a -> a
//                    .terms(TermsAggregation.of(t -> t
//                            .field("dataResource")
//                            .size(10)
//                    )
//            ));
//
//            // 执行查询
//            SearchResponse<Void> response = esClient.search(b -> b
//                    .index(todayIndex)
//                    .size(0)
//                    .aggregations(aggregations)
//                    .trackTotalHits(t -> t.enabled(true)), Void.class);
//
//            // 解析聚合结果
//            Map<Integer, Long> counts = new HashMap<>();
//            response.aggregations().get("by_resource").sterms().buckets().array().forEach(bucket -> {
//                int resource = Integer.parseInt(bucket.key());
//                counts.put(resource, bucket.docCount());
//            });
//
//            // 计算总量和百分比
//            long total = response.hits().total() != null ? response.hits().total().value() : 0;
//            long ckCount = counts.getOrDefault(0, 0L);
//            long xcCount = counts.getOrDefault(1, 0L);
//
//            AlarmCountVO result = new AlarmCountVO();
//            result.setCkCount((int) ckCount);
//            result.setXcCount((int) xcCount);
//            result.setCkPercentage(total > 0 ? (int) (ckCount * 100 / total) : 0);
//            result.setXcPercentage(total > 0 ? (int) (xcCount * 100 / total) : 0);
//
//            return result;
//        } catch (Exception e) {
//            logger.error("获取当日告警统计失败", e);
//            return new AlarmCountVO();
//        }
//    }
//
//    public List<AlarmCountVO> getAlarmStatistics(Date startDate, Date endDate, Integer type) {
//        try {
//            // 根据type确定时间粒度和日期范围
//            String interval;
//            String dateFormat;
//            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
//
//            if (type == 1) { // 近30天
//                calendar.add(Calendar.DAY_OF_MONTH, -30);
//                interval = "1d";
//                dateFormat = "yyyy-MM-dd";
//            } else if (type == 2) { // 近一年
//                calendar.add(Calendar.YEAR, -1);
//                interval = "1M";
//                dateFormat = "yyyy-MM";
//            } else { // 自定义日期
//                interval = "1d";
//                dateFormat = "yyyy-MM-dd";
//            }
//
//            Date actualStart = type == 3 ? startDate : calendar.getTime();
//            Date actualEnd = type == 3 ? endDate : new Date();
//
//            // 构建日期直方图聚合
//            Map<String, Aggregation> aggregations = new HashMap<>();
//            aggregations.put("by_date", Aggregation.of(a -> a
//                    .dateHistogram(DateHistogramAggregation.of(d -> d
//                            .field("SRC_ALARM_NTIME")
//                            .calendarInterval(interval)
//                            .format(dateFormat)
//                    )
//                    .aggregations("by_resource", Aggregation.of(agg -> agg
//                            .terms(TermsAggregation.of(t -> t.field("dataResource"))))
//            );
//
//            // 执行查询
//            SearchResponse<Void> response = esClient.search(b -> b
//                    .index("kafka_alarm_log-*")
//                    .size(0)
//                    .query(q -> q
//                            .range(r -> r
//                                    .field("SRC_ALARM_NTIME")
//                                    .gte(JsonData.of(actualStart))
//                                    .lte(JsonData.of(actualEnd)))
//                    )
//                    .aggregations(aggregations)
//                    , Void.class);
//
//            // 解析聚合结果
//            List<AlarmCountVO> results = new ArrayList<>();
//            response.aggregations().get("by_date").dhistogram().buckets().array().forEach(dateBucket -> {
//                AlarmCountVO vo = new AlarmCountVO();
//                vo.setCurrTime(dateBucket.keyAsString());
//
//                dateBucket.aggregations().get("by_resource").sterms().buckets().array().forEach(resourceBucket -> {
//                    int resource = Integer.parseInt(resourceBucket.key());
//                    long count = resourceBucket.docCount();
//
//                    if (resource == 0) {
//                        vo.setCkCount((int) count);
//                    } else if (resource == 1) {
//                        vo.setXcCount((int) count);
//                    }
//                });
//
//                results.add(vo);
//            });
//
//            return results;
//        } catch (Exception e) {
//            logger.error("获取多维度告警统计失败", e);
//            return Collections.emptyList();
//        }
//    }
}
