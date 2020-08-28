package com.cl.log.server.server;

import com.cl.log.config.model.LogFactory;
import com.cl.log.server.config.SpringContextUtil;
import com.cl.log.server.model.BizLog;
import com.cl.log.server.task.TaskCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务分发器.
 * TODO 针对不同的日志类型，调用对应的 Task 进行处理.
 *
 * @author leichu 2020-06-23.
 */
public class TaskDistributor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TaskDistributor.class);

	private LogFactory.Log log;

	public TaskDistributor(LogFactory.Log log) {
		this.log = log;
	}

	@Override
	public void run() {
		final LogFactory.Log.Category category = this.log.getCategory();
		switch (category) {
			case biz_log:
				SpringContextUtil.getBean(TaskCenter.class).put(BizLog.convert(this.log.getBizLog()));
				break;
			case perf_log:
				final LogFactory.PerfLog perfLog = this.log.getPerfLog();

				break;
			default:
				logger.error("日志类型异常！{}", category);
				break;
		}
	}
}
