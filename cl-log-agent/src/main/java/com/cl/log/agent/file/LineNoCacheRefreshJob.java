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

	private static final Map<String, String> map = Maps.newConcurrentMap();


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

	public static void refresh(String key, long no) {
		map.put(key, String.valueOf(no));
	}

	public static long getLineNo(String key) {
		if (map.containsKey(key)) {
			return Long.parseLong(map.get(key));
		}
		Object o = ZkRegister.getInstance().get(key);
		if (o == null) {
			ZkRegister.getInstance().set(key, "0");
			return 0L;
		}
		return Long.parseLong(o.toString());
	}

	public static void resetLineNo(String key) {
		ZkRegister.getInstance().set(key, "0");
		map.put(key, "0");
	}

}
