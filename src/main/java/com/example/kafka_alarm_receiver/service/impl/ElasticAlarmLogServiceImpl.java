package com.example.kafka_alarm_receiver.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.kafka_alarm_receiver.service.ElasticAlarmLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ElasticAlarmLogServiceImpl 是一个服务实现类，用于从 Elasticsearch 查询告警日志数据。
 * 提供按时间范围聚合的告警统计功能。
 */
@Service
@RequiredArgsConstructor
public class ElasticAlarmLogServiceImpl implements ElasticAlarmLogService {

    // 日期格式常量
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 注入的 Elasticsearch 客户端
    private final ElasticsearchClient client;

    /**
     * 根据给定的查询条件获取告警数量。
     *
     * @param query 查询构建器
     * @return 告警总数
     */
    private long fetchAlarmCount(Query.Builder query) throws IOException, ElasticsearchException {
        CountRequest.Builder builder = new CountRequest.Builder();
        builder.index("kafka_alarm_log-*");
        builder.query(query.build());

        CountResponse countResponse = client.count(builder.build());
        return countResponse.count();
    }

    /**
     * 获取 CK 类型的告警数量（DATA_RESOURCE = 0）。
     */
    @Override
    public long fetchCKAlarmCount() throws IOException, ElasticsearchException {
        Query.Builder query = new Query.Builder();
        query.term(term -> term.field("DATA_RESOURCE").value(0));
        return fetchAlarmCount(query);
    }

    /**
     * 获取 XC 类型的告警数量（DATA_RESOURCE = 1）。
     */
    @Override
    public long fetchXCAlarmCount() throws IOException, ElasticsearchException {
        Query.Builder query = new Query.Builder();
        query.term(term -> term.field("DATA_RESOURCE").value(1));
        return fetchAlarmCount(query);
    }

    /**
     * 准备时间范围和聚合粒度。
     *
     * @param type       时间类型（1:30天按天、2:一年按月、3:自定义、4:当天按小时）
     * @param startDate  起始时间
     * @param endDate    结束时间
     * @return DateRange 包含起止时间、聚合间隔、格式化工具等信息
     */
    private DateRange prepareDateRange(int type, Date startDate, Date endDate) {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date startLocatDate, endLocatDate;
        String startDateStr, endDateStr;
        CalendarInterval interval;
        String dateFormat;
        SimpleDateFormat boundSdf;

        switch (type) {
            case 1: // 近30天，按天聚合
                startLocatDate = Date.from(now.minusDays(30).atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Day;
                dateFormat = "MM-dd";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 2: // 近一年，按月聚合
                startLocatDate = Date.from(now.minusYears(1).atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Month;
                dateFormat = "MM月";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 3: // 自定义时间范围
                validateCustomDateRange(startDate, endDate);
                interval = (endDate.getTime() - startDate.getTime()) > 180L * 24 * 60 * 60 * 1000
                        ? CalendarInterval.Month : CalendarInterval.Day;
                startLocatDate = startDate;
                endLocatDate = endDate;
                dateFormat = (endDate.getTime() - startDate.getTime()) > 180L * 24 * 60 * 60 * 1000
                        ? "MM月" : "MM-dd";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 4: // 当天，按小时聚合
                LocalDateTime startOfDay = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
                startLocatDate = Date.from(startOfDay.atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Hour;
                dateFormat = "HH时";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            default:
                throw new IllegalArgumentException("不支持的 type 类型");
        }

        startDateStr = sdf.format(startLocatDate);
        endDateStr = sdf.format(endLocatDate);

        return new DateRange(startDateStr, endDateStr, interval, boundSdf);
    }

    /**
     * 按照指定的时间类型、资源类型获取告警数量的时间分布直方图。
     *
     * @param type         时间类型（1:30天按天、2:一年按月、3:自定义、4:当天按小时）
     * @param resourceType 数据源类型（0:CK, 1:XC）
     * @param startDate    起始时间（可选）
     * @param endDate      结束时间（可选）
     * @return 返回按时间分组的告警数量结果列表
     */
    public List<DateHistogramResult> fetchAlarmDateHistogram(int type, int resourceType, Date startDate, Date endDate)
            throws IOException, ElasticsearchException, ParseException {

        // 1. 构建时间范围
        DateRange dateRange = this.prepareDateRange(type, startDate, endDate);

        // 2. 创建搜索请求
        SearchRequest.Builder request = new SearchRequest.Builder();
        request.index("kafka_alarm_log-*").size(0); // 不返回具体文档

        // 3. 构建查询条件：时间范围 + 数据源类型
        Query.Builder query = new Query.Builder();
        BoolQuery.Builder bool = new BoolQuery.Builder();
        // 3.2查询时间范围内的告警
        bool.must(m -> m.range(r -> r.date(dt -> dt.field("SRC_EVENTTIME").gte(dateRange.startDate()).lt(dateRange.endDate()))));
        // 3.3查询对应的数据源
        bool.must(m -> m.term(t -> t.field("DATA_RESOURCE").value(resourceType)));
        query.bool(bool.build());
        request.query(query.build());

        // 4. 构建聚合：基于事件时间的日期直方图聚合
        Aggregation.Builder aggregation = new Aggregation.Builder();
        DateHistogramAggregation.Builder builder = new DateHistogramAggregation.Builder();
        builder.field("SRC_EVENTTIME")
                .minDocCount(0)
                .calendarInterval(dateRange.interval)
                .extendedBounds(eb -> eb
                        .min(FieldDateMath.of(fd -> fd.expr(dateRange.startDate())))
                        .max(FieldDateMath.of(fd -> fd.expr(dateRange.endDate())))
                )
                .format(DATE_FORMAT);
        aggregation.dateHistogram(builder.build());
        request.aggregations("timeAgg", aggregation.build());

        // 5. 执行查询
        SearchResponse<Map> response = client.search(request.build(), Map.class);

        // 6. 解析响应中的聚合结果
        List<DateHistogramBucket> buckets = response.aggregations()
                .get("timeAgg")
                .dateHistogram()
                .buckets()
                .array();

        ArrayList<DateHistogramResult> result = new ArrayList<>();
        for (var bucket : buckets) {
            String time = bucket.keyAsString();
            Long count = bucket.docCount();
            Date parse = dateRange.getDateFormat().parse(time);
            String formatTime = dateRange.boundSdf().format(parse);
            result.add(new DateHistogramResult(time, count, parse, formatTime));
        }
        return result;
    }

    /**
     * 验证自定义时间段是否提供了起止时间。
     */
    private void validateCustomDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("自定义时间段需提供 startDate 和 endDate");
        }
    }

    /**
     * 封装时间范围参数。
     */
    public record DateRange(String startDate, String endDate, CalendarInterval interval, SimpleDateFormat boundSdf) {
        public SimpleDateFormat getDateFormat() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 封装时间聚合结果。
     */
    public record DateHistogramResult(String time, Long count, Date parse, String formatTime) {
    }
}
