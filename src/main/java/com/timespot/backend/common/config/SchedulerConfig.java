package com.timespot.backend.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(Math.max(4, Runtime.getRuntime().availableProcessors()));
        taskScheduler.setThreadNamePrefix("journey-notification-");
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}