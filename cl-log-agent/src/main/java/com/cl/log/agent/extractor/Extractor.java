
package com.cl.log.agent.extractor;

import com.cl.log.config.model.LogFactory;

import java.util.List;

/**
 * 日志提取器.
 *
 * @author leichu 2020-08-27.
 */
public interface Extractor {

	/**
	 * 提取.
	 *
	 * @param content content.
	 * @return LogFactory.Log 列表.
	 */
	List<LogFactory.Log> extract(List<String> content);

}
