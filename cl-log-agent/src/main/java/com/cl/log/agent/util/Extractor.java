
package com.cl.log.agent.util;

import com.cl.log.config.model.LogFactory;

/**
 * 日志提取器.
 *
 * @author leichu 2020-08-27.
 */
public interface Extractor {

	LogFactory.Log extract(String content);

	class BizExtractor implements Extractor {

		@Override
		public LogFactory.Log extract(String content) {
			return LogFactory.Log.newBuilder().setCategory(LogFactory.Log.Category.biz_log)
					.setBizLog(
							LogFactory.BizLog.newBuilder().setHost("1").setBiz("业务日志").setMsg("出现异常了").build()
					).build();
		}
	}

	class PerfExtractor implements Extractor {

		@Override
		public LogFactory.Log extract(String content) {
			return null;
		}
	}

	class AccessExtractor implements Extractor {

		@Override
		public LogFactory.Log extract(String content) {
			return null;
		}
	}

}
