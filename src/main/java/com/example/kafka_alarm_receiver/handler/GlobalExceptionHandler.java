package com.example.kafka_alarm_receiver.handler;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<CommonErrorResponse> handleElasticsearchException(ElasticsearchException ex) {
        // 返回自定义错误响应

        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(IOException.class)
    public ResponseEntity<CommonErrorResponse> handleIOException(IOException ex) {
        // 返回自定义错误响应

        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 处理所有其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse> handleGenericException(Exception ex) {
        // 捕获所有其他异常，并返回统一格式的错误信息
        CommonErrorResponse.CommonErrorResponseBuilder builder = CommonErrorResponse.builder();
        builder.status(500L);
        builder.message(ex.getMessage());
        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
