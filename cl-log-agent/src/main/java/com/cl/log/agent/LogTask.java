package com.cl.log.agent;

import com.cl.log.agent.client.ChannelHandlerContextHolder;
import com.cl.log.agent.client.NettyClient;
import com.cl.log.agent.config.LogFileConfig;
import com.cl.log.agent.file.FileListener;
import com.cl.log.agent.util.Extractor;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	private long lineNo;

	private NettyClient client;
	private String nettyClientId;

	public LogTask(LogFileConfig.LogFileCfg config) {
		nettyClientId = UUID.randomUUID().toString();
		this.config = config;
		extractor = LogFactoryUtils.parseExtractor(this.config.getType());
//		cacheKey = this.config.getPath();
		cacheKey = DigestUtils.md5DigestAsHex(this.config.getPath().getBytes());
		initLineNo();
		initNettyClient();
		System.out.println("》》》》》》》》》》 客户端");
	}

	public void initNettyClient() {
		String availableUrl = ZkRegister.getInstance().getAvailableUrl();
		client = new NettyClient(nettyClientId, availableUrl.split(":")[0], Integer.parseInt(availableUrl.split(":")[1]));
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			client.start();
		});
	}

	private void initLineNo() {
		Object o = ZkRegister.getInstance().get(cacheKey);
		if (o == null) {
			lineNo = 0L;
			ZkRegister.getInstance().set(cacheKey, lineNo);
			return;
		}
		lineNo = (long) o;
	}

	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	@Override
	public void run() {
		refreshLineCache();
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

	private void refreshLineCache() {
		scheduledExecutorService.scheduleAtFixedRate(() -> ZkRegister.getInstance().set(cacheKey, lineNo), 0, 1, TimeUnit.SECONDS);
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
		LogFactory.Log log = extractor.extract("你好");
		ChannelHandlerContext channelHandlerContext = ChannelHandlerContextHolder.getChannelHandlerContext(nettyClientId);
		if (null != channelHandlerContext){
			Channel channel = channelHandlerContext.channel();
			channel.writeAndFlush(log);
			return;
		}
		initNettyClient();
		channelHandlerContext = ChannelHandlerContextHolder.getChannelHandlerContext(Thread.currentThread().getName());
		Channel channel = channelHandlerContext.channel();
		channel.writeAndFlush(log);
	}

}
