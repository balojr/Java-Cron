package com.slime.cronjobs.service;

import com.slime.cronjobs.config.CronConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CronService {
    private static final String CRON_JOB = "Cron Job Executed Successfully!!!";

    public String executeCronJob() {
        log.info(CRON_JOB);
        return CRON_JOB;
    }

}
