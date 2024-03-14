package com.slime.cronjobs.config;

import com.slime.cronjobs.service.CronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class CronConfig {

    private final CronService cronService;

    @Autowired
    public CronConfig(CronService cronService) {
        this.cronService = cronService;
    }

    @Scheduled(fixedDelay = 2000)
    public void scheduleFixedDelayTask() {
        log.info("Fixed delay task - " + System.currentTimeMillis() / 2000);
        cronService.executeCronJob();
    }
}
