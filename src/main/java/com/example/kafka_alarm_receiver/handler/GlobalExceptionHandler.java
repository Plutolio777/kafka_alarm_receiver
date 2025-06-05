package com.example.kafka_alarm_receiver.handler;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * GlobalExceptionHandler 是一个全局异常处理器，用于统一处理整个应用中的异常。
 * 它使用 `@RestControllerAdvice` 注解，表示它是一个 Spring MVC 全局异常处理器，并且返回值直接作为响应体。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Elasticsearch 相关的异常。
     *
     * @param ex 捕获到的 ElasticsearchException 异常
     * @return 返回统一格式的错误响应和 HTTP 状态码 500
     */
    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<CommonErrorResponse> handleElasticsearchException(ElasticsearchException ex) {
        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理 IO 相关的异常。
     *
     * @param ex 捕获到的 IOException 异常
     * @return 返回统一格式的错误响应和 HTTP 状态码 500
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<CommonErrorResponse> handleIOException(IOException ex) {
        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理所有未被其他方法捕获的异常。
     *
     * @param ex 捕获到的通用 Exception 异常
     * @return 返回统一格式的错误响应和 HTTP 状态码 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse> handleGenericException(Exception ex) {
        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
