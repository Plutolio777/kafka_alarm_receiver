package com.example.kafka_alarm_receiver.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.PutTemplateRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class EsInitializer {

    @Value("${es.index-template}")
    private String templateName;

    @Value("${es.index-pattern}")
    private String indexPattern;

    private final ElasticsearchClient client;

    private final Logger logger = LoggerFactory.getLogger(EsInitializer.class);

    @PostConstruct
    public void init() {
        try {
            log.info("run EsInitializer with index pattern {} and template {}", indexPattern, templateName);
            // 1.检查ES健康请情况
            if (checkEsHealth()) {
                // 2.设置索引模版
                createInitialIndexWithAlias();
                // 3.尝试设置当天索引
                setIndex();
            }
        } catch (Exception e) {
            logger.error("ES 初始化失败", e);
        }
    }

    private boolean checkEsHealth()  throws Exception  {
        try {
            client.cluster().health();
            return true;
        } catch (ElasticsearchException e) {
            log.error("connect es error: ", e);

        }
        return false;

    }

    private void createInitialIndexWithAlias() throws Exception {

        PutTemplateRequest.Builder builder = new PutTemplateRequest.Builder();
        builder
                .name("kafka_alarm_log_template")
                .indexPatterns("kafka_alarm_log-*")
                .settings(r->r
                        .numberOfShards("1")
                        .numberOfReplicas("1")
                ).mappings(mp->mp
                        .source(s->s.enabled(true))
                        .properties("SRC_ACKNOWLEDGEMENTTIMESTAMP", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARM_NTIME", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("SRC_PERCEIVEDSEVERITY", Property.of(p -> p.integer(i -> i)))
                        .properties("SRC_MANU_ALARMFLAG", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_EVENTTIME", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("SRC_LOCATIONINFO", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARMCODE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARMID", Property.of(p -> p.long_(l -> l)))
                        .properties("SRC_NMS_TYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_SYNC_FLAG", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARMSUBTYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARM_NID", Property.of(p -> p.keyword(k -> k)))
                        .properties("sCT", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARM_PROBLEM", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_SPECIALTY", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ID", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_SYSTEM_TYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("sPS", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARM_NO", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_REGION", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_SYSTEM_DN", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ACKSTATUS", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_IS_TEST", Property.of(p -> p.boolean_(b -> b)))
                        .properties("SRC_ALARMTITLE", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO9", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_IP", Property.of(p -> p.ip(i -> i)))
                        .properties("SRC_EQUIPMENTNAME", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INERTTIME", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("SRC_SOURCE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARMTYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_CLEARANCEREPORTFLAG", Property.of(p -> p.boolean_(b -> b)))
                        .properties("SRC_IPADDRESS", Property.of(p -> p.ip(i -> i)))
                        .properties("SRC_STATE", Property.of(p -> p.integer(i -> i)))
                        .properties("SRC_NAME", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_vendor_ALARMID", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_INFO8", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO7", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO6", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO5", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_NECLASS", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_CLEARANCETIMESTAMP", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_INFO4", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO3", Property.of(p -> p.text(t -> t)))
                        .properties("sEN", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO2", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_INFO1", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_RESTORETYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ORG_CLR_OPTR", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_APP_ID", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARMSID", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_VENDOR", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ALARM_NTYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_NETTYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_SENDTIME", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("SRC_MANU_ALARMTYPE", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ADDITIONALTEXT", Property.of(p -> p.text(t -> t)))
                        .properties("SRC_SYNC_NO", Property.of(p -> p.keyword(k -> k)))
                        .properties("SRC_ADDITIONALINFO", Property.of(p -> p.text(t -> t)))
                        .properties("DATA_RESOURCE", Property.of(p->p.integer(t->t)))
                        .properties("APP_NAME", Property.of(p->p.keyword(t->t)))
                );
        client.indices().putTemplate(builder.build());
        logger.info("✅创建模版{}成功", "kafka_alarm_log_template");
    }

    private void setIndex() throws IOException {
        CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
        builder.index("<kafka_alarm_log-{now/M}>").aliases("kafka_alarm_log", r->r);
        try {
            client.indices().create(builder.build());
            logger.info("✅创建当天索引成功");
        } catch (ElasticsearchException e) {
            if (Objects.requireNonNull(e.error().reason()).contains("already exists")) {
                logger.info("✅创建当天索引成功: 当天索引已存在");
            }
        }

    }
}
