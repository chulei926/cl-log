package com.cl.log.server.task;

import com.cl.log.config.common.SpringContextWrapper;
import com.cl.log.config.model.LogFactory;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.EsIndex;
import com.cl.log.server.persistence.AccessLogRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tomcat访问日志job.
 * <pre>
 *     处理缓冲队列中的遗留数据.
 * </pre>
 *
 * @author leichu 2020-07-09.
 */
public class AccessLogJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(AccessLog.class);

	public static void handle(List<LogFactory.TomcatAccessLog> accessLogs) {
		if (CollectionUtils.isEmpty(accessLogs)) {
			return;
		}
		List<AccessLog> accessLogList = accessLogs.stream().map(AccessLog::convert).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(accessLogList)) {
			return;
		}
		Map<String, List<AccessLog>> map = accessLogList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(AccessLog::getDate4Day));
		if (CollectionUtils.isEmpty(map)) {
			return;
		}
		AccessLogRepository accessLogRepository = SpringContextWrapper.getBean(AccessLogRepository.class);
		map.forEach((k, v) -> {
			String index = String.format("%s%s@%s", AccessLog.INDEX_PREFIX, v.get(0).getBiz(), k);
			accessLogRepository.batchInsert(new EsIndex(index), v);
		});
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("Tomcat访问日志job执行");
		TaskCenter taskCenter = SpringContextWrapper.getBean(TaskCenter.class);
		List<LogFactory.TomcatAccessLog> accessLogs = taskCenter.getAccessLogs();
		handle(accessLogs);
	}
}
