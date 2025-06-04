package com.example.kafka_alarm_receiver.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.example.kafka_alarm_receiver.service.impl.ElasticAlarmLogServiceImpl;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface ElasticAlarmLogService {
    long fetchCKAlarmCount() throws IOException, ElasticsearchException;
    long fetchXCAlarmCount() throws IOException, ElasticsearchException;
    List<ElasticAlarmLogServiceImpl.DateHistogramResult> fetchAlarmDateHistogram(int type, int resourceType, Date startDate, Date endDate) throws IOException, ElasticsearchException, ParseException;
}
