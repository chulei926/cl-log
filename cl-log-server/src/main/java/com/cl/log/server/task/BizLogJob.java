package com.cl.log.server.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务日志job.
 * <pre>
 *     处理缓冲队列中的遗留数据.
 * </pre>
 *
 * @author leichu 2020-07-09.
 */
public class BizLogJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(BizLogJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		logger.debug("业务日志job执行");

//		if (TaskCenter.BIZ_LOG_QUEUE.size() < 1) {
//			return;
//		}
//		List<LogFactory.Log> list = Lists.newArrayList();
//		TaskCenter.BIZ_LOG_QUEUE.drainTo(list);
//		if (CollectionUtils.isEmpty(list)) {
//			return;
//		}
//		System.out.println(Thread.currentThread().getName() + " >>>>> " + LocalDateTime.now() + " >>>>> " + list.size());
//		Map<Class, List<ILog>> map = Maps.newHashMap();
//		list.forEach(log -> {
//			String key = log.getClass().getName();
//			map.putIfAbsent(log.getClass(), Lists.newArrayList());
//			map.get(key).add(log);
//		});

	}
}
