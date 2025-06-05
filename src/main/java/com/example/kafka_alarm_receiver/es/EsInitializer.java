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

/**
 * EsInitializer 是一个 Spring 组件，用于在应用启动时初始化 Elasticsearch 配置。
 * 包括创建索引模板和当天的索引。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EsInitializer {

    // 从配置中注入索引模板名称
    @Value("${es.index-template}")
    private String templateName;

    // 从配置中注入索引模式（例如 kafka_alarm_log-*）
    @Value("${es.index-pattern}")
    private String indexPattern;

    // 注入 Elasticsearch 客户端
    private final ElasticsearchClient client;

    // 日志记录器
    private final Logger logger = LoggerFactory.getLogger(EsInitializer.class);

    /**
     * 应用启动后执行初始化逻辑。
     * 1. 检查 Elasticsearch 健康状态；
     * 2. 创建索引模板；
     * 3. 创建当天的索引（按月滚动）。
     */
    @PostConstruct
    public void init() {
        try {
            log.info("run EsInitializer with index pattern {} and template {}", indexPattern, templateName);
            if (checkEsHealth()) {
                createInitialIndexWithAlias(); // 创建索引模板
                setIndex();                    // 创建当天索引
            }
        } catch (Exception e) {
            logger.error("ES 初始化失败", e);
        }
    }

    /**
     * 检查 Elasticsearch 集群是否健康。
     *
     * @return 如果集群健康返回 true，否则返回 false
     */
    private boolean checkEsHealth() throws Exception {
        try {
            client.cluster().health();
            return true;
        } catch (ElasticsearchException e) {
            log.error("connect es error: ", e);
            return false;
        }
    }

    /**
     * 创建 Elasticsearch 索引模板，定义字段映射和设置。
     * 模板匹配 `kafka_alarm_log-*` 格式的索引。
     */
    private void createInitialIndexWithAlias() throws Exception {
        PutTemplateRequest.Builder builder = new PutTemplateRequest.Builder();
        builder
                .name("kafka_alarm_log_template")
                .indexPatterns("kafka_alarm_log-*")
                .settings(r -> r
                        .numberOfShards("1")         // 分片数
                        .numberOfReplicas("1")       // 副本数
                ).mappings(mp -> mp
                        .source(s -> s.enabled(true)) // 启用 _source 字段
                        // 定义各个字段的类型和映射
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
                        .properties("DATA_RESOURCE", Property.of(p -> p.integer(t -> t)))
                        .properties("APP_NAME", Property.of(p -> p.keyword(t -> t)))
                );

        client.indices().putTemplate(builder.build());
        logger.info("✅ 创建模版{}成功", "kafka_alarm_log_template");
    }

    /**
     * 创建当天的索引（格式为 kafka_alarm_log-YYYY.MM），并添加别名。
     * 如果索引已存在则不会重复创建。
     */
    private void setIndex() throws IOException {
        CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
        builder.index("<kafka_alarm_log-{now/M}>").aliases("kafka_alarm_log", r -> r);

        try {
            client.indices().create(builder.build());
            logger.info("✅ 创建当天索引成功");
        } catch (ElasticsearchException e) {
            // 如果索引已经存在，则忽略错误
            if (Objects.requireNonNull(e.error().reason()).contains("already exists")) {
                logger.info("✅ 创建当天索引成功: 当天索引已存在");
            } else {
                throw e; // 抛出其他异常
            }
        }
    }
}
