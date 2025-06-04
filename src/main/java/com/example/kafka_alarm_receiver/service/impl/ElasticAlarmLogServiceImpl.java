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

@Service
@RequiredArgsConstructor
public class ElasticAlarmLogServiceImpl implements ElasticAlarmLogService {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final ElasticsearchClient client;

    private long fetchAlarmCount(Query.Builder query) throws IOException, ElasticsearchException {
        CountRequest.Builder builder = new CountRequest.Builder();
        builder.index("kafka_alarm_log-*");
        builder.query(query.build());

        CountResponse countResponse = client.count(builder.build());
        return countResponse.count();
    }

    @Override
    public long fetchCKAlarmCount() throws IOException, ElasticsearchException {
        Query.Builder query = new Query.Builder();
        query.term(term -> term.field("DATA_RESOURCE").value(0));
        return fetchAlarmCount(query);
    }


    @Override
    public long fetchXCAlarmCount() throws IOException, ElasticsearchException {
        Query.Builder query = new Query.Builder();
        query.term(term -> term.field("DATA_RESOURCE").value(1));
        return fetchAlarmCount(query);
    }

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
            case 1: // 近30天，按天
                startLocatDate = Date.from(now.minusDays(30).atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Day;
                dateFormat = "MM-dd";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 2: // 近一年，按月
                startLocatDate = Date.from(now.minusYears(1).atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Month;
                dateFormat = "MM月";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 3: // 自定义
                validateCustomDateRange(startDate, endDate);
                interval = (endDate.getTime() - startDate.getTime()) > 180L * 24 * 60 * 60 * 1000
                        ? CalendarInterval.Month : CalendarInterval.Day;
                startLocatDate = startDate;
                endLocatDate = endDate;
                dateFormat = (endDate.getTime() - startDate.getTime()) > 180L * 24 * 60 * 60 * 1000
                        ? "MM月" : "MM-dd";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            case 4: // 当天，按小时
                LocalDateTime startOfDay = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
                startLocatDate = Date.from(startOfDay.atZone(zoneId).toInstant());
                endLocatDate = Date.from(now.atZone(zoneId).toInstant());
                interval = CalendarInterval.Hour;
                dateFormat = "HH时";
                boundSdf = new SimpleDateFormat(dateFormat);
                break;
            default:
                throw new IllegalArgumentException("不支持的type类型");
        }

        startDateStr = sdf.format(startLocatDate);
        endDateStr = sdf.format(endLocatDate);

        return new DateRange(startDateStr, endDateStr, interval, boundSdf);
    }


    public List<DateHistogramResult> fetchAlarmDateHistogram(int type, int resourceType, Date startDate, Date endDate) throws IOException, ElasticsearchException, ParseException {
        // 1. 创建查询请求
        DateRange dateRange = this.prepareDateRange(type, startDate, endDate);
        SearchRequest.Builder request = new SearchRequest.Builder();

        // 2.设置查询索引和文本数
        request.index("kafka_alarm_log-*").size(0);

        // 3.创建查询
        Query.Builder query = new Query.Builder();
        // 3.1创建bool查询
        BoolQuery.Builder bool = new BoolQuery.Builder();
        // 3.2查询时间范围内的告警
        bool.must(m -> m.range(r -> r.date(dt -> dt.field("SRC_EVENTTIME").gte(dateRange.startDate()).lt(dateRange.endDate()))));
        // 3.3查询对应的数据源
        bool.must(m -> m.term(t -> t.field("DATA_RESOURCE").value(resourceType)));
        query.bool(bool.build());
        request.query(query.build());

        // 4.创建聚合
        Aggregation.Builder aggregation = new Aggregation.Builder();
        // 4.1创建日期直方图聚合
        DateHistogramAggregation.Builder builder = new DateHistogramAggregation.Builder();
        builder.field("SRC_EVENTTIME");
        builder.minDocCount(0);
        builder.calendarInterval(dateRange.interval);
        builder.extendedBounds(
            eb -> eb
                .min(FieldDateMath.of(fd -> fd.expr(dateRange.startDate())))
                .max(FieldDateMath.of(fd -> fd.expr(dateRange.endDate())))
        );
        builder.format(DATE_FORMAT);
        aggregation.dateHistogram(builder.build());
        request.aggregations("timeAgg", aggregation.build());

        SearchResponse<Map> response = client.search(request.build(), Map.class);

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

    private void validateCustomDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("自定义时间段需提供 startDate 和 endDate");
        }
    }

    public record DateRange(String startDate, String endDate, CalendarInterval interval, SimpleDateFormat boundSdf) {
        public SimpleDateFormat getDateFormat() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    public record DateHistogramResult(String time, Long count, Date parse, String formatTime) {
    }

}
