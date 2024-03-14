package com.slime.cronjobs.service;

import com.slime.cronjobs.config.CronConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CronService class.
 */
@Service
@Slf4j
public class CronService {
    private static final String CRON_JOB_1 = "Cron Job Executed Successfully!!!";

    private static final String CRON_JOB_2 = "Cron Job 2 Executed Successfully!!!";

    /**
     * Executes the cron job and logs its success.
     *
     * @return a string message indicating the successful execution of the cron job
     */
    public String executeCronJob() {
        log.info(CRON_JOB_1);
        return CRON_JOB_1;
    }

        /**
     * Executes the second cron job and logs its success.
     *
     * @return a string message indicating the successful execution of the second cron job
     */
    public String executeCronJob2() {
        log.info(CRON_JOB_2);
        return CRON_JOB_2;
    }

    /**
     * Returns the delay for the next execution of the cron job.
     *
     * @return a long value representing the delay in milliseconds
     */
    public long getDelay() {
        return 2000L; // returns a fixed delay of 2000 milliseconds
    }
}
