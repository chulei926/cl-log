package com.cl.log.agent.config;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * 日志文件配置类.
 *
 * @author leichu 2020-08-26.
 */
public class LogFileConfig {

	private static final List<LogFileCfg> configs = Lists.newArrayList();

	private static final String rootPath;

	static {
		Properties properties = new Properties();
		try {
			properties.load(LogFileConfig.class.getClassLoader().getResourceAsStream("log_cfg.properties"));
		} catch (IOException e) {
			throw new RuntimeException("log_cfg.properties load failed.");
		}
		rootPath = properties.getProperty("root_path");
		final Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement().toString();
			String name;
			LogFileCfg cfg = new LogFileCfg();
			if (key.startsWith("biz")) {
				name = key.replaceFirst("biz\\.", "");
				cfg.setName(name).setType("biz").setPath(Paths.get(rootPath, properties.getProperty(key)).toString());
			} else if (key.startsWith("perf")) {
				name = key.replaceFirst("perf\\.", "");
				cfg.setName(name).setType("perf").setPath(Paths.get(rootPath, properties.getProperty(key)).toString());
			} else if (key.startsWith("access")) {
				name = key.replaceFirst("access\\.", "");
				cfg.setName(name).setType("access").setPath(Paths.get(rootPath, properties.getProperty(key)).toString());
			} else if (key.startsWith("nginx")) {
				name = key.replaceFirst("nginx\\.", "");
				cfg.setName(name).setType("nginx").setPath(Paths.get(rootPath, properties.getProperty(key)).toString());
			} else {
				continue;
			}
			configs.add(cfg);
		}
	}

	public static List<LogFileCfg> getConfigs() {
		return configs;
	}

	public static int getLogFileCount() {
		return configs.size();
	}

	public static class LogFileCfg implements Serializable {

		private static final long serialVersionUID = -633436491426743643L;

		private String name;
		private String path;
		private String type;

		public String getName() {
			return name;
		}

		public LogFileCfg setName(String name) {
			this.name = name;
			return this;
		}

		public String getPath() {
			return path;
		}

		public LogFileCfg setPath(String path) {
			this.path = path;
			return this;
		}

		public String getType() {
			return type;
		}

		public LogFileCfg setType(String type) {
			this.type = type;
			return this;
		}
	}


}
