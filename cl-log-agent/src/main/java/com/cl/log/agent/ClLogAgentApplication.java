package com.cl.log.agent;

import com.cl.log.agent.config.LogFileConfig;
import com.cl.log.agent.file.LineNoCacheRefreshJob;
import com.cl.log.agent.task.LogTask;
import com.cl.log.agent.task.RemainLogHandlerBeforeApplicationDestroy;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@SpringBootApplication(scanBasePackages = "com.cl.log")
@EnableAsync
public class ClLogAgentApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClLogAgentApplication.class);

	private static ConfigurableApplicationContext applicationContext;

	static {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(() -> {
			if (applicationContext != null) {
				logger.warn("正在关闭程序释放资源，请稍等....");
				RemainLogHandlerBeforeApplicationDestroy handler = applicationContext.getBean(RemainLogHandlerBeforeApplicationDestroy.class);
				handler.handle();
				applicationContext.close();
				logger.warn("cl-log客户端程序已关闭！");
			}
		}));
	}

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(ClLogAgentApplication.class, args);
		// 初始化行号 刷新任务
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
			logger.info("行号刷新定时任务已启动！");
		} catch (SchedulerException se) {
			throw new RuntimeException("行号刷新定时任务启动失败！", se);
		}
	}

}
