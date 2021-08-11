package com.cl.log.server.task;

import com.cl.log.config.model.LogFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应用程序关闭前，对遗留的日志进行处理。
 *
 * @author leichu 2020-10-13.
 */
@Slf4j
@Component
public class RemainLogHandlerBeforeApplicationDestroy {

	@Resource
	private TaskCenter taskCenter;

	/**
	 * 处理剩余的 log 。
	 */
	public void handle() {
		try {
			List<LogFactory.TomcatAccessLog> accessLogs = taskCenter.getAccessLogs();
			AccessLogJob.handle(accessLogs);
			log.warn("遗留日志（{}）处理完成！", "Tomcat访问日志");
		} catch (Exception e) {
			log.error("遗留日志（{}）处理异常！", "Tomcat访问日志", e);
		}
		// TODO
		List<LogFactory.BizLog> bizLogs = taskCenter.getBizLogs();
		List<LogFactory.PerfLog> perfLogs = taskCenter.getPerfLogs();
	}

}
