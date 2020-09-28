package com.cl.log.agent.util;

import com.cl.log.agent.extractor.AccessExtractor;
import com.cl.log.agent.extractor.BizExtractor;
import com.cl.log.agent.extractor.Extractor;
import com.cl.log.agent.extractor.PerfExtractor;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 日志工厂工具类.
 *
 * @author leichu 2020-08-26.
 */
public class LogFactoryUtils {

	private static final Logger logger = LoggerFactory.getLogger(LogFactoryUtils.class);

	static Map<String, Class<?>> logExtractorMap = Maps.newConcurrentMap();

	static {
		logExtractorMap.put("biz", BizExtractor.class);
		logExtractorMap.put("perf", PerfExtractor.class);
		logExtractorMap.put("access", AccessExtractor.class);
	}

	public static Extractor parseExtractor(String type) {
		if (!logExtractorMap.containsKey(type)) {
			throw new IllegalArgumentException("不支持的文件类型：" + type);
		}
		Class<?> clazz = logExtractorMap.get(type);
		try {
			return (Extractor) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("创建日志提取器实例异常！", e);
		}
	}

	/**
	 * 创建日志记录文件.
	 * <pre>
	 *     如果出现异常，循环创建3遍。
	 * </pre>
	 *
	 * @param file logRecordFile.
	 */
	public static void touchLogRecordFile(File file) {
		if (null == file || file.exists()) {
			return;
		}
		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				FileUtils.touch(file);
				success = true;
				break;
			} catch (Exception e) {
				logger.error("创建日志记录文件[{}]异常！", file.getAbsolutePath(), e);
			}
		}
		if (!success) {
			throw new RuntimeException("创建日志记录文件异常！文件路径：" + file.getAbsolutePath());
		}
	}

	/**
	 * 读取日志记录文件到list
	 *
	 * @param file file.
	 * @return List.
	 */
	public static List<String> readLogRecordFile2Lines(File file) {
		if (null == file || !file.exists()) {
			throw new RuntimeException("日志记录文件不存在！");
		}
		try {
			return FileUtils.readLines(file, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("日志记录文件读取异常！" + file.getAbsolutePath(), e);
		}
	}

	public static void appendExtracted2LogRecordFile(File file, String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return;
		}
		try {
			FileUtils.writeStringToFile(file, fileName, StandardCharsets.UTF_8, true);
		} catch (Exception e) {
			throw new RuntimeException("日志记录文件追加异常！" + file.getAbsolutePath() + " " + fileName, e);
		}
	}


}
