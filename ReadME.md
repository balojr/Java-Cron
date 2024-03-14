# Enable Support for Scheduling

To enable support for scheduling tasks and the `@Scheduled` annotation in Spring, we can use the Java enable-style annotation:

```java
@Configuration
@EnableScheduling
public class SpringConfig {
    //...
}
```
Conversely, we can do the same in XML:

```xml
<task:annotation-driven>
```

# Schedule a Task at Fixed Delay

Let’s start by configuring a task to run after a fixed delay:

```java
@Scheduled(fixedDelay = 1000)
public void scheduleFixedDelayTask() {
    log.info("Fixed delay task - " + System.currentTimeMillis() / 1000);
}
```

In this case, the duration between the end of the last execution and the start of the next execution is fixed. The task always waits until the previous one is finished.  This option should be used when it’s mandatory that the previous execution is completed before running again.

This option should be used when it’s mandatory that the previous execution is completed before running again. 

# Schedule a Task at a Fixed Rate

Let’s now execute a task at a fixed interval of time:

```java
@Scheduled(fixedRate = 1000)
public void scheduleFixedRateTask() {
    log.info("Fixed rate task - " + System.currentTimeMillis() / 1000);
}
```

This option should be used when each execution of the task is independent.  
Note that scheduled tasks don’t run in parallel by default. So even if we used fixedRate, the next task won’t be invoked until the previous one is done.  
If we want to support parallel behavior in scheduled tasks, we need to add the @Async annotation:

```java
@EnableAsync
public class ScheduledFixedRateExample {
    @Async
    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        log.info("Fixed rate task async - " + System.currentTimeMillis() / 1000);
        Thread.sleep(2000);
    }
}
```
Now this asynchronous task will be invoked each second, even if the previous task isn’t done.

# Fixed Rate vs Fixed Delay

We can run a scheduled task using Spring’s @Scheduled annotation, but based on the properties fixedDelay and fixedRate, the nature of execution changes.

The fixedDelay property makes sure that there is a delay of n millisecond between the finish time of an execution of a task and the start time of the next execution of the task.

This property is specifically useful when we need to make sure that only one instance of the task runs all the time. For dependent jobs, it is quite helpful. 

The fixedRate property runs the scheduled task at every n millisecond. It doesn’t check for any previous executions of the task.

This is useful when all executions of the task are independent. If we don’t expect to exceed the size of the memory and the thread pool, fixedRate should be quite handy.

Although, if the incoming tasks do not finish quickly, it’s possible they end up with “Out of Memory exception”.

# Schedule a Task With Initial Delay

Next, let’s schedule a task with a delay (in milliseconds):

```java
@Scheduled(fixedDelay = 1000, initialDelay = 1000)
public void scheduleFixedRateWithInitialDelayTask() {
 
    long now = System.currentTimeMillis() / 1000;
    log.info("Fixed rate task with one second initial delay - " + now);
}
```

Note how we’re using both fixedDelay as well as initialDelay in this example. The task will be executed the first time after the initialDelay value, and it will continue to be executed according to the fixedDelay.

This option is convenient when the task has a setup that needs to be completed.

# Schedule a Task Using Cron Expressions

Sometimes delays and rates are not enough, and we need the flexibility of a cron expression to control the schedule of our tasks:

```java
@Scheduled(cron = "0 15 10 15 * ?")
public void scheduleTaskUsingCronExpression() {
 
    long now = System.currentTimeMillis() / 1000;
    log.info("schedule tasks using cron jobs - " + now);
}
```

Note that in this example, we’re scheduling a task to be executed at 10:15 AM on the 15th day of every month.

By default, Spring will use the server’s local time zone for the cron expression. However, we can use the zone attribute to change this timezone:

```java
@Scheduled(cron = "0 15 10 15 * ?", zone = "Europe/Paris")
```

With this configuration, Spring will schedule the annotated method to run at 10:15 AM on the 15th day of every month in Paris time.

# Parametrizing the Schedule

Hardcoding these schedules is simple, but we usually need to be able to control the schedule without re-compiling and re-deploying the entire app.

We’ll make use of Spring Expressions to externalize the configuration of the tasks, and we’ll store these in properties files.

A fixedDelay task:

```java
@Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
```

A fixedRate task:
```java
@Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
```

A cron expression based task:
```java
@Scheduled(cron = "${cron.expression}")
```

# Setting Delay or Rate Dynamically at Runtime

Normally, all the properties of the @Scheduled annotation are resolved and initialized only once at Spring context startup.

Therefore, changing the fixedDelay or fixedRate values at runtime isn’t possible when we use @Scheduled annotation in Spring.

However, there is a workaround. Using Spring’s SchedulingConfigurer provides a more customizable way to give us the opportunity of setting the delay or rate dynamically.

Let’s create a Spring configuration, DynamicSchedulingConfig, and implement the SchedulingConfigurer interface:

```java
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    @Autowired
    private TickService tickService;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
          new Runnable() {
              @Override
              public void run() {
                  tickService.tick();
              }
          },
          new Trigger() {
              @Override
              public Date nextExecutionTime(TriggerContext context) {
                  Optional<Date> lastCompletionTime =
                    Optional.ofNullable(context.lastCompletionTime());
                  Instant nextExecutionTime =
                    lastCompletionTime.orElseGet(Date::new).toInstant()
                      .plusMillis(tickService.getDelay());
                  return Date.from(nextExecutionTime);
              }
          }
        );
    }

}
```

As we notice, with the help of the ScheduledTaskRegistrar#addTriggerTask method, we can add a Runnable task and a Trigger implementation to recalculate the nextExecutionTime after the end of each execution.

Additionally, we annotate our DynamicSchedulingConfig with @EnableScheduling to make the scheduling work.

As a result, we scheduled the TickService#tick method to run it after each amount of delay, which is determined dynamically at runtime by the getDelay method.

# Running Tasks in Parallel

By default, Spring uses a local single-threaded scheduler to run the tasks. As a result, even if we have multiple @Scheduled methods, they each need to wait for the thread to complete executing a previous task.

If our tasks are truly independent, it’s more convenient to run them in parallel. For that, we need to provide a TaskScheduler that better suits our needs:

```java
@Bean
public TaskScheduler  taskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    return threadPoolTaskScheduler;
}
```

In the above example, we configured the TaskScheduler with a pool size of five, but keep in mind that the actual configuration should be fine-tuned to one’s specific needs.

# Using Spring Boot

If we use Spring Boot, we can make use of an even more convenient approach to increase the scheduler’s pool size.

It’s simply enough to set the spring.task.scheduling.pool.size property:
spring.task.scheduling.pool.size=5


