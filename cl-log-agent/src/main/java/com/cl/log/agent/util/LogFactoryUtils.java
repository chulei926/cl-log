package com.cl.log.agent.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 日志工厂工具类.
 *
 * @author leichu 2020-08-26.
 */
public class LogFactoryUtils {

	static Map<String, Class<?>> logExtractorMap = Maps.newConcurrentMap();

	static {
		logExtractorMap.put("biz", Extractor.BizExtractor.class);
		logExtractorMap.put("perf", Extractor.PerfExtractor.class);
		logExtractorMap.put("access", Extractor.AccessExtractor.class);
	}

	public static Extractor parseExtractor(String type){
		if (!logExtractorMap.containsKey(type)){
			return null;
		}
		Class<?> clazz = logExtractorMap.get(type);
		try {
			return  (Extractor) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("创建日志提取器实例异常！", e);
		}
	}

}
