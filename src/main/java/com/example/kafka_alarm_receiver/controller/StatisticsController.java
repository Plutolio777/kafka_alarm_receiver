package com.example.kafka_alarm_receiver.controller;

import com.example.kafka_alarm_receiver.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/kafka/resource")
public class StatisticsController {

//    private final StatisticsService statisticsService;
//
//    @Autowired
//    public StatisticsController(StatisticsService statisticsService) {
//        this.statisticsService = statisticsService;
//    }
//
//    @GetMapping("/count")
//    public AlarmCountVO getTodayAlarmCount() {
//        return statisticsService.getTodayAlarmCount();
//    }
//
//    @PostMapping("/select")
//    public List<AlarmCountVO> getAlarmStatistics(
//            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
//            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
//            @RequestParam("type") Integer type) {
//        return statisticsService.getAlarmStatistics(startDate, endDate, type);
//    }
}
