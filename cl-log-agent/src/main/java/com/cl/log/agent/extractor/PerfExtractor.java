package com.cl.log.agent.extractor;

import com.cl.log.agent.config.Alias;
import com.cl.log.config.model.LogFactory;

import java.util.List;

/**
 * 姓名日志提取器.
 *
 * @author leichu 2020-09-01.
 */
@Alias("perf")
public class PerfExtractor implements Extractor {

	@Override
	public List<LogFactory.Log> extract(List<String> content) {
		return null;
	}

}
