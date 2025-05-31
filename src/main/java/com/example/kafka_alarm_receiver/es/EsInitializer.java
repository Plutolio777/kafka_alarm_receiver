package com.example.kafka_alarm_receiver.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.ilm.PutLifecycleRequest;
import co.elastic.clients.elasticsearch.ilm.PutLifecycleResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest;
import co.elastic.clients.elasticsearch.indices.PutTemplateRequest;
import co.elastic.clients.elasticsearch.indices.PutTemplateResponse;
import co.elastic.clients.elasticsearch.indices.put_index_template.IndexTemplateMapping;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class EsInitializer {

    @Value("${es.index-template}")
    private String templateName;

    @Value("${es.index-pattern}")
    private String indexPattern;

    @Autowired
    private RestClient restClient; // 用于底层调用 ILM、Alias 等

    @Autowired
    private ElasticsearchClient client;

    private final Logger logger = LoggerFactory.getLogger(EsInitializer.class);

    @PostConstruct
    public void init() {
        try {
            log.info("run EsInitializer with index pattern {} and template {}", indexPattern, templateName);
            checkEsHealth();
            createInitialIndexWithAlias();
            setIndex();
        } catch (Exception e) {
            logger.error("ES 初始化失败", e);
        }
    }

    private void checkEsHealth()  throws Exception  {
        HealthResponse health = client.cluster().health();
        log.info(health.status().toString());
    }

//    private void createIlmPolicy() throws Exception {
//
//        PutLifecycleRequest.Builder builder = new PutLifecycleRequest.Builder();
//        builder
//                .name("kafka_alarm_log_ilm_policy")
//                .policy(p -> p.phases(
//                                ph -> ph.hot(
//                                                h -> h.actions(
//                                                        a -> a.rollover(
//                                                                ro -> ro.maxAge(Time.of(t -> t.time("1d")))
//                                                        )
//                                                )
//                                        )
//                                        .delete(dl -> dl.minAge(Time.of(t -> t.time("365d"))).actions(at -> at.delete(dd -> dd)))
//                        )
//                );
////        System.out.println(builder.build().toString());
//        PutLifecycleResponse kafkaAlarmLogIlmPolicy = client.ilm()
//                .putLifecycle(builder.build());
//        logger.info("✅ 创建 ILM 策略成功: {}", kafkaAlarmLogIlmPolicy.toString());
//    }

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
                        .properties("src_acknowledgementtimestamp", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarm_ntime", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("src_perceivedseverity", Property.of(p -> p.integer(i -> i)))
                        .properties("src_manu_alarmflag", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_eventtime", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("src_locationinfo", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarmcode", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarmid", Property.of(p -> p.long_(l -> l)))
                        .properties("src_nms_type", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_sync_flag", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarmsubtype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarm_nid", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarm_problem", Property.of(p -> p.text(t -> t)))
                        .properties("src_specialty", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_id", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_system_type", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_region", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_system_dn", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_ackstatus", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_is_test", Property.of(p -> p.boolean_(b -> b)))
                        .properties("src_alarmtitle", Property.of(p -> p.text(t -> t)))
                        .properties("src_ip", Property.of(p -> p.ip(i -> i)))
                        .properties("src_equipmentname", Property.of(p -> p.text(t -> t)))
                        .properties("src_inerttime", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("src_source", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarmtype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_clearancereportflag", Property.of(p -> p.boolean_(b -> b)))
                        .properties("src_ipaddress", Property.of(p -> p.ip(i -> i)))
                        .properties("src_state", Property.of(p -> p.integer(i -> i)))
                        .properties("src_name", Property.of(p -> p.text(t -> t)))
                        .properties("src_vendor_alarmid", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_info8", Property.of(p -> p.text(t -> t)))
                        .properties("src_neclass", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_clearancetimestamp", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_restoretype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_vendor", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_alarm_ntype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_nettype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_sendtime", Property.of(p -> p.date(d -> d.format("yyyy-MM-dd HH:mm:ss"))))
                        .properties("src_manu_alarmtype", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_additionaltext", Property.of(p -> p.text(t -> t)))
                        .properties("src_sync_no", Property.of(p -> p.keyword(k -> k)))
                        .properties("src_additionalinfo", Property.of(p -> p.text(t -> t)))
                        .properties("data_resource", Property.of(p->p.integer(t->t)))
                );
        PutTemplateResponse putTemplateResponse = client.indices().putTemplate(builder.build());
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
