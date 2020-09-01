package com.cl.log.agent.task;

import com.cl.log.agent.client.ChannelHandlerContextHolder;
import com.cl.log.agent.client.NettyClient;
import com.cl.log.agent.config.LogFileConfig;
import com.cl.log.agent.extractor.Extractor;
import com.cl.log.agent.file.FileListener;
import com.cl.log.agent.file.LineNoCacheRefreshJob;
import com.cl.log.agent.util.LogFactoryUtils;
import com.cl.log.config.model.LogFactory;
import com.cl.log.config.register.ZkRegister;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 日志收集任务.
 *
 * @author leichu 2020-08-26.
 */
public class LogTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(LogTask.class);

	private final LogFileConfig.LogFileCfg config;
	private final Extractor extractor;

	private final String cacheKey;

	private NettyClient client;
	private final String nettyClientId;

	public LogTask(LogFileConfig.LogFileCfg config) {
		nettyClientId = UUID.randomUUID().toString();
		this.config = config;
		extractor = LogFactoryUtils.parseExtractor(this.config.getType());
		cacheKey = DigestUtils.md5DigestAsHex(this.config.getPath().getBytes());
		initNettyClient();
	}

	public void initNettyClient() {
		String availableUrl = ZkRegister.getInstance().getAvailableUrl();
		client = new NettyClient(nettyClientId, availableUrl.split(":")[0], Integer.parseInt(availableUrl.split(":")[1]));
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> client.start());
	}

	@Override
	public void run() {
		Path curPath = Paths.get(config.getPath());
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
			new FileAlterationMonitor(TimeUnit.MILLISECONDS.toMillis(500), observer).start();
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
	 *     3. 获取传输通道 Channel ，传输数据。
	 * </pre>
	 *
	 * @param file file.
	 */
	private void parseFile(File file) {
		Long lineNo = LineNoCacheRefreshJob.getLineNo(cacheKey);
		List<LogFactory.Log> logs = null;
		Path path = file.toPath();
		try (Stream<String> linesStream = Files.lines(path)) {
			List<String> lines = linesStream.skip(lineNo).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(lines)){
				return;
			}
			logs = extractor.extract(lines);
			lineNo += lines.size();
			LineNoCacheRefreshJob.refresh(cacheKey, lineNo);
		} catch (Exception e) {
			throw new RuntimeException("文件解析异常！", e);
		}
		if (CollectionUtils.isEmpty(logs)) {
			return;
		}
		ChannelHandlerContext channelHandlerContext = ChannelHandlerContextHolder.getChannelHandlerContext(nettyClientId);
		Channel channel = channelHandlerContext.channel();
		logs.forEach(channel::writeAndFlush);
		logger.info("日志已发送，总量：{}", logs.size());
	}
}