package com.cl.log.server.task;

import com.cl.log.server.model.ILog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 处理缓冲队列中的遗留数据.
 *
 * @author leichu 2020-07-09.
 */
public class LogJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println(Thread.currentThread().getName() + " >>>>> " + LocalDateTime.now());
		// TODO 处理缓冲队列中的剩余数据
		System.out.println(TaskCenter.LOG_QUEUE.size());
		if (TaskCenter.LOG_QUEUE.size() < 1) {
			return;
		}
		List<ILog> list = Lists.newArrayList();
		TaskCenter.LOG_QUEUE.drainTo(list);
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		Map<Class, List<ILog>> map = Maps.newHashMap();
		list.forEach(log -> {
			String key = log.getClass().getName();
			map.putIfAbsent(log.getClass(), Lists.newArrayList());
			map.get(key).add(log);
		});

	}
}
