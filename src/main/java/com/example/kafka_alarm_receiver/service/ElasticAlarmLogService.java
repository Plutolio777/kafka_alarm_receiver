package com.example.kafka_alarm_receiver.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.example.kafka_alarm_receiver.service.impl.ElasticAlarmLogServiceImpl;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * ElasticAlarmLogService 是一个服务接口，用于定义与 Elasticsearch 告警日志相关的查询操作。
 * 主要提供以下功能：
 * 1. 获取 CK/XC 类型的告警总数；
 * 2. 按时间范围和资源类型获取告警数量的时间分布直方图数据。
 */
public interface ElasticAlarmLogService {

    /**
     * 获取 CK 类型（DATA_RESOURCE = 0）的今日告警总数。
     *
     * @return 返回 CK 类型的告警数量
     * @throws IOException 如果发生 I/O 错误
     * @throws ElasticsearchException 如果 Elasticsearch 查询失败
     */
    long fetchCKAlarmCount() throws IOException, ElasticsearchException;

    /**
     * 获取 XC 类型（DATA_RESOURCE = 1）的今日告警总数。
     *
     * @return 返回 XC 类型的告警数量
     * @throws IOException 如果发生 I/O 错误
     * @throws ElasticsearchException 如果 Elasticsearch 查询失败
     */
    long fetchXCAlarmCount() throws IOException, ElasticsearchException;

    /**
     * 按指定时间范围、资源类型获取告警数量的时间分布统计。
     *
     * @param type         时间类型（1:近30天按天、2:近一年按月、3:自定义、4:当天按小时）
     * @param resourceType 数据源类型（0:CK, 1:XC）
     * @param startDate    起始时间（可选）
     * @param endDate      结束时间（可选）
     * @return 返回按时间分组的告警数量结果列表
     * @throws IOException 如果发生 I/O 错误
     * @throws ElasticsearchException 如果 Elasticsearch 查询失败
     * @throws ParseException 如果日期解析失败
     */
    List<ElasticAlarmLogServiceImpl.DateHistogramResult> fetchAlarmDateHistogram(
            int type, int resourceType, Date startDate, Date endDate)
            throws IOException, ElasticsearchException, ParseException;
}
