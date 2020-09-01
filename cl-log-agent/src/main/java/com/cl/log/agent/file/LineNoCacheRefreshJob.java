package com.cl.log.agent.file;

import com.cl.log.config.register.ZkRegister;
import com.google.common.collect.Maps;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 行号自动刷新job。
 * <pre>
 *     每秒执行一次刷新操作.
 * </pre>
 *
 * @author leichu 2020-09-01.
 */
public class LineNoCacheRefreshJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(LineNoCacheRefreshJob.class);

	private static final Map<String, Long> map = Maps.newConcurrentMap();


	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (CollectionUtils.isEmpty(map)) {
			return;
		}
		map.forEach((key, value) -> {
			ZkRegister.getInstance().set(key, value);
		});
		logger.info("行号已刷新！总量:{}", map.size());
	}

	public static void refresh(String key, Long no) {
		map.put(key, no);
	}

	public static Long getLineNo(String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		Long lineNo = 0L;
		Object o = ZkRegister.getInstance().get(key);
		if (o == null) {
			ZkRegister.getInstance().set(key, lineNo);
			return lineNo;
		}
		lineNo = (Long) o;
		return lineNo;
	}

}
