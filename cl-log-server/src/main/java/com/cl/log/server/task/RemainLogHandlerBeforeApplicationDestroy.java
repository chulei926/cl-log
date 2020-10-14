package com.cl.log.server.task;

import com.cl.log.config.model.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应用程序关闭前，对遗留的日志进行处理。
 *
 * @author leichu 2020-10-13.
 */
@Component
public class RemainLogHandlerBeforeApplicationDestroy {

	private static final Logger logger = LoggerFactory.getLogger(RemainLogHandlerBeforeApplicationDestroy.class);

	@Resource
	private TaskCenter taskCenter;

	/**
	 * 处理剩余的 log 。
	 */
	public void handle() {
		try {
			List<LogFactory.TomcatAccessLog> accessLogs = taskCenter.getAccessLogs();
			AccessLogJob.handle(accessLogs);
			logger.warn("遗留日志（{}）处理完成！", "Tomcat访问日志");
		} catch (Exception e){
			logger.error("遗留日志（{}）处理异常！", "Tomcat访问日志", e);
		}
		// TODO
		List<LogFactory.BizLog> bizLogs = taskCenter.getBizLogs();
		List<LogFactory.PerfLog> perfLogs = taskCenter.getPerfLogs();
	}

}
