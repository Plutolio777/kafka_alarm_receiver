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

@Configuration
@Slf4j
public class ElasticClientConfig {


    @Value("${es.host}")
    private String host;

    @Value("${es.port}")
    private Integer port;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;

    @Value("${es.schema}")
    private String schema;

    @Bean
    public RestClient restClient() {
        log.info("create es base client http://{}:{} with username {} passowrd {}", host, port, username, password);

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema));
        if (!"".equals(this.username)) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }
        return builder.build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                        if (!"".equals(username)) {
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        }
                        if ("https".equals(schema)) {
                            // 跳出SSL证书检查 （生产环境禁用）
                            try {
                                SSLContext sslContext = SSLContexts.custom()
                                        .loadTrustMaterial(null, (certificate, authType) -> true)  // 信任所有证书
                                        .build();
                                httpClientBuilder.setSSLContext(sslContext);
                            } catch (NoSuchAlgorithmException e) {
                                log.error("Failed to create SSLContext: NoSuchAlgorithmException", e);
                            } catch (KeyManagementException e) {
                                log.error("Failed to create SSLContext: KeyManagementException", e);
                            } catch (KeyStoreException e) {
                                log.error("Failed to create SSLContext: KeyStoreException", e);
                            }
                            // 跳过域名检查
                            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        }
                        return httpClientBuilder;
                    }
                );

        RestClient restClient = builder.build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
