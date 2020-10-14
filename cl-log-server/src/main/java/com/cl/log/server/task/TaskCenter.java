package com.cl.log.server.task;

import com.cl.log.config.model.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Component
public class TaskCenter {

	private static final Logger logger = LoggerFactory.getLogger(TaskCenter.class);

	/**
	 * 业务日志缓冲队列.
	 */
	private static final LinkedBlockingQueue<LogFactory.BizLog> BIZ_LOG_QUEUE = new LinkedBlockingQueue<>();
	/**
	 * 性能日志缓冲队列.
	 */
	private static final LinkedBlockingQueue<LogFactory.PerfLog> PERF_LOG_QUEUE = new LinkedBlockingQueue<>();
	/**
	 * Tomcat访问日志缓冲队列.
	 */
	private static final LinkedBlockingQueue<LogFactory.TomcatAccessLog> ACCESS_LOG_QUEUE = new LinkedBlockingQueue<>();

	private static final int PERF_LOG_JOB_INTERVAL = 1;
	private static final int BIZ_LOG_JOB_INTERVAL = 1;
	private static final int ACCESS_LOG_JOB_INTERVAL = 1;

	static {
		try {
			// 性能日志
			JobDetail perfLogJob = newJob(PerfLogJob.class).withIdentity("perfLogJob", "gp1").withDescription("性能日志job").build();
			Trigger perfLogJobTrigger = newTrigger().withIdentity("perfLogJobTrigger", "gp1").withDescription("性能日志job触发器").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(PERF_LOG_JOB_INTERVAL).repeatForever()).build();

			// 业务日志
			JobDetail bizLogJob = newJob(BizLogJob.class).withIdentity("bizLogJob", "gp2").withDescription("业务日志job").build();
			Trigger bizLogJobTrigger = newTrigger().withIdentity("bizLogJobTrigger", "gp2").withDescription("业务日志job触发器").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(BIZ_LOG_JOB_INTERVAL).repeatForever()).build();

			// Tomcat访问日志
			JobDetail accessLogJob = newJob(AccessLogJob.class).withIdentity("accessLogJob", "gp3").withDescription("Tomcat访问日志job").build();
			Trigger accessLogJobTrigger = newTrigger().withIdentity("accessLogJobTrigger", "gp3").withDescription("Tomcat访问日志job触发器").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(ACCESS_LOG_JOB_INTERVAL).repeatForever()).build();

			Map<JobDetail, Set<? extends Trigger>> triggersAndJobs = Maps.newConcurrentMap();
			triggersAndJobs.put(perfLogJob, Sets.newHashSet(perfLogJobTrigger));
			triggersAndJobs.put(bizLogJob, Sets.newHashSet(bizLogJobTrigger));
			triggersAndJobs.put(accessLogJob, Sets.newHashSet(accessLogJobTrigger));

			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.scheduleJobs(triggersAndJobs, false);
			scheduler.start();
			logger.info("性能日志（{}s）、业务日志（{}s）、访问日志（{}s） 定时任务均已启动！", PERF_LOG_JOB_INTERVAL, BIZ_LOG_JOB_INTERVAL, ACCESS_LOG_JOB_INTERVAL);
		} catch (SchedulerException se) {
			throw new RuntimeException("服务端定时任务启动失败！", se);
		}
	}

	public void put2BizLogQueue(LogFactory.BizLog log) {
		if (null == log) {
			return;
		}
		try {
			BIZ_LOG_QUEUE.put(log);
		} catch (Exception ex) {
			throw new RuntimeException("向 业务日志 队列中添加元素出现异常！", ex);
		}
	}

	public void put2PerfLogQueue(LogFactory.PerfLog log) {
		if (null == log) {
			return;
		}
		try {
			PERF_LOG_QUEUE.put(log);
		} catch (Exception ex) {
			throw new RuntimeException("向 性能日志 队列中添加元素出现异常！", ex);
		}
	}

	public void put2AccessLogQueue(LogFactory.TomcatAccessLog log) {
		if (null == log) {
			return;
		}
		try {
			ACCESS_LOG_QUEUE.put(log);
		} catch (Exception ex) {
			throw new RuntimeException("向 访问日志 队列中添加元素出现异常！", ex);
		}
	}

	public List<LogFactory.BizLog> getBizLogs() {
		List<LogFactory.BizLog> list = Lists.newArrayList();
		if (BIZ_LOG_QUEUE.size() < 1) {
			return list;
		}
		BIZ_LOG_QUEUE.drainTo(list);
		return list;
	}

	public List<LogFactory.PerfLog> getPerfLogs() {
		List<LogFactory.PerfLog> list = Lists.newArrayList();
		if (PERF_LOG_QUEUE.size() < 1) {
			return list;
		}
		PERF_LOG_QUEUE.drainTo(list);
		return list;
	}

	public List<LogFactory.TomcatAccessLog> getAccessLogs() {
		List<LogFactory.TomcatAccessLog> list = Lists.newArrayList();
		if (ACCESS_LOG_QUEUE.size() < 1) {
			return list;
		}
		ACCESS_LOG_QUEUE.drainTo(list);
		return list;
	}


}
