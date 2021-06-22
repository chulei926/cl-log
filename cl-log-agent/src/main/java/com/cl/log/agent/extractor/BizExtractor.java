package com.cl.log.agent.extractor;

import com.cl.log.agent.config.Alias;
import com.cl.log.config.model.LogFactory;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 业务日志提取器.
 *
 * @author leichu 2020-09-01.
 */
@Alias("biz")
public class BizExtractor implements Extractor {

	@Override
	public List<LogFactory.Log> extract(List<String> content) {
		LogFactory.Log log = LogFactory.Log.newBuilder().setCategory(LogFactory.Log.Category.biz_log)
				.setBizLog(
						LogFactory.BizLog.newBuilder().setHost("1").setBiz("业务日志").setMsg("出现异常了").build()
				).build();
		return Lists.newArrayList(log);
	}

}
