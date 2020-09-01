
package com.cl.log.agent.extractor;

import com.cl.log.config.model.LogFactory;

import java.util.List;

/**
 * 日志提取器.
 *
 * @author leichu 2020-08-27.
 */
public interface Extractor {

	default LogFactory.Log testLog(){
		LogFactory.Log log =  LogFactory.Log.newBuilder().setCategory(LogFactory.Log.Category.biz_log)
				.setBizLog(
						LogFactory.BizLog.newBuilder().setHost("1").setBiz("业务日志").setMsg("出现异常了").build()
				).build();
		return log;
	}

	/**
	 * 提取.
	 *
	 * @param content content.
	 * @return LogFactory.Log 列表.
	 */
	List<LogFactory.Log> extract(List<String> content);

}
