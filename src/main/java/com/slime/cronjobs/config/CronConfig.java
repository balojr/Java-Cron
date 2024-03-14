package com.slime.cronjobs.config;

import com.slime.cronjobs.service.CronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


 /**
 * CronConfig class.
 */
@Configuration
@EnableScheduling
@Slf4j
@EnableAsync
public class CronConfig {

    private final CronService cronService;

    /**
     * Constructor for CronConfig.
     *
     * @param cronService the CronService to be used by this CronConfig
     */
    @Autowired
    public CronConfig(CronService cronService) {
        this.cronService = cronService;
    }

        /**
     * Scheduled task that is executed every 2000 milliseconds.
     * It logs the current time and executes the cron job.
     */
    @Scheduled(fixedDelay = 2000)
    public void scheduleFixedDelayTask() {
        log.info("Fixed delay task - " + System.currentTimeMillis() / 1000);
        cronService.executeCronJob();
    }

    /**
     * Scheduled task that is executed at a fixed rate every 2000 milliseconds.
     * It logs the current time and executes the second cron job.
     */
    @Scheduled(fixedRate = 2000)
    public void scheduleFixedRateTask() {
        log.info("Fixed rate task - " + System.currentTimeMillis() / 2000);
        cronService.executeCronJob2();
    }

    /**
     * Asynchronously scheduled task that is executed at a fixed rate every 1000 milliseconds.
     * It logs the current time and then sleeps for 2000 milliseconds.
     * @throws InterruptedException if the thread sleep is interrupted
     */
    @Async
    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        log.info("Fixed rate task async - " + System.currentTimeMillis() / 1000);
        Thread.sleep(2000);
    }

    /**
     * Scheduled task that is executed using a cron expression.
     * It logs the current time. The cron expression is set to trigger the task every day at 11:56 PM.
     */
    @Scheduled(cron = "0 55 23 * * ?")
    public void scheduleTaskUsingCronExpression() {
        long now = System.currentTimeMillis() / 1000;
        log.info("schedule tasks using cron jobs - " + now);
    }
}

