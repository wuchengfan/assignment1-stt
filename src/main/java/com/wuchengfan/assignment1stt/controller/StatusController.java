package com.wuchengfan.assignment1stt.controller;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wuchengfan.assignment1stt.service.StatisticsService;

@RestController
public class StatusController {

    private final StatisticsService statisticsService;

    public StatusController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getServerUptime() {

        Instant serverStart = Instant.ofEpochMilli(
                ManagementFactory.getRuntimeMXBean().getStartTime());

        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(serverStart, now).toMillis() / 1000.0;

        return new UptimeResponse(
                serverStart.toString(),
                now.toString(),
                uptimeSeconds);
    }

    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getGlobalStats() {

        return new GlobalStatsResponse(
                statisticsService.getInputTokens(),
                statisticsService.getOutputTokens());
    }

    private record UptimeResponse(
            String utcServerStart,
            String utcNow,
            double serverUptimeSeconds) {
    }

    private record GlobalStatsResponse(
            long inputTokens,
            long outputTokens) {
    }
}