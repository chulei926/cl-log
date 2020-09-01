package com.cl.log.agent;

import com.cl.log.agent.config.LogFileConfig;
import com.cl.log.agent.file.LineNoCacheRefreshJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@SpringBootApplication
@EnableAsync
public class ClLogAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClLogAgentApplication.class, args);

		initLineNoRefreshJob();
		// 采用多线程的方式，启动 收集任务，有多少个需要收集的日志，就起多少个线程
		int count = LogFileConfig.getLogFileCount();
		ExecutorService executorService = Executors.newFixedThreadPool(count);
		LogFileConfig.getConfigs().forEach(config -> executorService.execute(new LogTask(config)));
	}

	private static void initLineNoRefreshJob() {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobDetail job = newJob(LineNoCacheRefreshJob.class).withIdentity("LineNoCacheRefreshJob", "gp").build();

			Trigger trigger = newTrigger().withIdentity("LineNoCacheRefreshJobTrigger", "gp").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever()).build();

			scheduler.scheduleJob(job, trigger);
			scheduler.start();
		} catch (SchedulerException se) {
			throw new RuntimeException("行号刷新定时任务启动失败！", se);
		}
	}

}
