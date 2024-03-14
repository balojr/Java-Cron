package com.slime.cronjobs.config;

import com.slime.cronjobs.service.CronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration class for dynamic scheduling.
 */
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    private final CronService cronService;

    /**
     * Constructor for DynamicSchedulingConfig.
     *
     * @param cronService the CronService to be used by this DynamicSchedulingConfig
     */
    @Autowired
    public DynamicSchedulingConfig(CronService cronService) {
        this.cronService = cronService;
    }

    /**
     * Provides a single-threaded executor for scheduling tasks.
     *
     * @return an Executor instance
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Configures tasks for scheduling.
     *
     * @param taskRegistrar the ScheduledTaskRegistrar to be used for configuring tasks
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
                new Runnable() {
                    @Override
                    public void run() {
                        cronService.executeCronJob();
                    }
                },
                new Trigger() {
                    /**
                     * Determines the next execution time for the task.
                     *
                     * @param context the TriggerContext to be used for determining the next execution time
                     * @return a Date representing the next execution time
                     */
                    @Override
                    public Date nextExecutionTime(TriggerContext context) {
                        Optional<Date> lastCompletionTime =
                                Optional.ofNullable(context.lastCompletionTime());
                        Instant nextExecutionTime =
                                lastCompletionTime.orElseGet(Date::new).toInstant()
                                        .plusMillis(cronService.getDelay());
                        return Date.from(nextExecutionTime);
                    }

                    @Override
                    public Instant nextExecution(TriggerContext triggerContext) {
                        return null;
                    }
                }
        );
    }
}
