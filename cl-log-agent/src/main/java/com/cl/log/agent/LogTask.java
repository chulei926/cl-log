package com.cl.log.agent;

import com.cl.log.agent.client.NettyClient;
import com.cl.log.agent.config.LogFileConfig;
import com.cl.log.agent.file.FileListener;
import com.cl.log.agent.util.Extractor;
import com.cl.log.agent.util.LogFactoryUtils;
import com.cl.log.config.model.LogFactory;
import com.cl.log.config.register.ZkRegister;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * 日志收集任务.
 *
 * @author leichu 2020-08-26.
 */
public class LogTask implements Runnable {

	private final LogFileConfig.LogFileCfg config;
	private final Extractor extractor;

	public LogTask(LogFileConfig.LogFileCfg config) {
		this.config = config;
		extractor = LogFactoryUtils.parseExtractor(this.config.getType());
	}

	LoadingCache<String, Long> lineCache = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build(
					new CacheLoader<String, Long>() {
						public Long load(String key) {
							return 0L;
						}
					});

	@Override
	public void run() {
		Path curPath = Paths.get(config.getPath());
		long interval = TimeUnit.SECONDS.toMillis(1);
		FileAlterationObserver observer = new FileAlterationObserver(
				curPath.getParent().toString(),
				FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".log")),
				null
		);
		observer.addListener(new FileListener((file) -> {
			if (file.getName().equals(curPath.getFileName().toString())) {
				parseFile(file);
			}
		}));
		try {
			new FileAlterationMonitor(interval, observer).start();
		} catch (Exception e) {
			throw new RuntimeException("文件监控程序启动异常！", e);
		}
	}


	/**
	 * 解析文件.
	 *
	 * <pre>
	 *     1. 缓存每次读取到的行号，下一次读取的时候，从指定行号开始向下读取。
	 *     2. 将读取到的内容封装成 Log 对象。
	 *     3. 启动 netty 客户端，传输数据。
	 * </pre>
	 *
	 * @param file file.
	 */
	private void parseFile(File file) {
		LogFactory.Log log = extractor.extract(null);
		String availableUrl = ZkRegister.getInstance().getAvailableUrl();
		NettyClient client = new NettyClient(availableUrl.split(":")[0], Integer.parseInt(availableUrl.split(":")[1]));
		client.start(log);

	}
}
