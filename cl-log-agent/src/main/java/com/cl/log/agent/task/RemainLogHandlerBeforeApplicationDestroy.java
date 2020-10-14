package com.cl.log.agent.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 应用程序关闭前，对遗留的日志进行处理。
 *
 * @author leichu 2020-10-13.
 */
@Component
public class RemainLogHandlerBeforeApplicationDestroy {

	private static final Logger logger = LoggerFactory.getLogger(RemainLogHandlerBeforeApplicationDestroy.class);

	/**
	 * 处理剩余的 log 。
	 */
	public void handle() {
		logger.error("haha ...");
	}

}
