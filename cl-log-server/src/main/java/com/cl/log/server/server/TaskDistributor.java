package com.cl.log.server.server;

import com.cl.log.config.common.SpringContextWrapper;
import com.cl.log.config.model.LogFactory;
import com.cl.log.server.task.TaskCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务分发器.
 * <pre>
 *     针对不同的日志类型，调用对应的 Task 进行处理.
 * </pre>
 *
 * @author leichu 2020-06-23.
 */
public class TaskDistributor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TaskDistributor.class);

	private final LogFactory.Log log;

	public TaskDistributor(LogFactory.Log log) {
		this.log = log;
		logger.info("收到消息：{}", this.log == null ? "未知" : this.log.getCategory());
	}

	@Override
	public void run() {
		final LogFactory.Log.Category category = this.log.getCategory();
		TaskCenter taskCenter = SpringContextWrapper.getBean(TaskCenter.class);
		switch (category) {
			case biz_log:
				taskCenter.put2BizLogQueue(this.log.getBizLog());
				break;
			case perf_log:
				taskCenter.put2PerfLogQueue(this.log.getPerfLog());
				break;
			case tomcat_access_log:
				taskCenter.put2AccessLogQueue(this.log.getTomcatAccessLog());
				break;
			default:
				logger.error("日志类型异常！{}", category);
				break;
		}
	}
}
