package com.example.kafka_alarm_receiver.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * ElasticClientConfig 是一个 Spring 配置类，用于创建和配置 Elasticsearch 客户端。
 * 支持基本认证和 HTTPS/SSL 配置（测试环境下信任所有证书）。
 */
@Configuration
@Slf4j
public class ElasticClientConfig {

    // 从 application.properties 或 application.yml 中注入 Elasticsearch 配置
    @Value("${es.host}")
    private String host;

    @Value("${es.port}")
    private Integer port;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;

    @Value("${es.schema}")
    private String schema; // 协议：http 或 https

    /**
     * 创建一个低级别的 RestClient Bean，用于与 Elasticsearch 进行基础通信。
     *
     * @return 配置好的 RestClient 实例
     */
    @Bean
    public RestClient restClient() {
        log.info("create es base client http://{}:{} with username {} password {}", host, port, username, password);

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema));

        // 如果配置了用户名，则添加基本认证
        if (!"".equals(this.username)) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        return builder.build();
    }

    /**
     * 创建并配置高级别的 ElasticsearchClient Bean，用于执行类型安全的 Elasticsearch 操作。
     *
     * @return 配置好的 ElasticsearchClient 实例
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    // 设置认证信息（如果存在）
                    if (!"".equals(username)) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }

                    // 如果使用 HTTPS，配置 SSL 上下文以跳过证书验证（仅限测试环境）
                    if ("https".equals(schema)) {
                        try {
                            SSLContext sslContext = SSLContexts.custom()
                                    .loadTrustMaterial(null, (certificate, authType) -> true)  // 信任所有证书
                                    .build();
                            httpClientBuilder.setSSLContext(sslContext);
                        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                            log.error("Failed to create SSLContext", e);
                        }

                        // 跳过域名检查（生产环境应禁用）
                        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    }

                    return httpClientBuilder;
                });

        RestClient restClient = builder.build();

        // 使用 Jackson JSON 映射器创建传输层
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // 返回最终的 ElasticsearchClient
        return new ElasticsearchClient(transport);
    }
}
