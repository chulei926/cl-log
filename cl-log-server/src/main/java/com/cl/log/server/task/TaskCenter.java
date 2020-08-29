package com.cl.log.server.task;

import com.cl.log.server.model.ILog;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 任务中心.
 * <p>
 * 每隔 1 秒 检查一下缓冲队列，如果有未处理的日志，持久化到 ES。
 *
 * @author leichu 2020-06-23.
 */
//@Component
public class TaskCenter {

	protected static final BlockingQueue<ILog> LOG_QUEUE = new LinkedBlockingQueue();

	static {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobDetail job = newJob(LogJob.class).withIdentity("logJob", "gp").build();

			Trigger trigger = newTrigger().withIdentity("logTrigger", "gp").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(1).repeatForever()).build();

			scheduler.scheduleJob(job, trigger);
			scheduler.start();
		} catch (SchedulerException se) {
			throw new RuntimeException("服务端定时任务启动失败！", se);
		}
	}

	public void put(ILog log) {
		try {
			LOG_QUEUE.put(log);
		} catch (InterruptedException ex) {
			throw new RuntimeException("向队列中添加元素出现异常！", ex);
		}
	}

}
