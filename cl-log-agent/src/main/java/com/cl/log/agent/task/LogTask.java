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
import com.cl.log.config.utils.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
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
@Slf4j
public class LogTask implements Runnable {

	private static final String LOG_RECORD_FILE = "_logRecord.txt";

	private final LogFileConfig.LogFileCfg config;
	private final Extractor extractor;

	private final String cacheKey;
	private final String nettyClientId;
	private NettyClient client;

	public LogTask(LogFileConfig.LogFileCfg config) {
		nettyClientId = UUID.randomUUID().toString();
		this.config = config;
		extractor = LogFactoryUtils.parseExtractor(this.config.getType());
		cacheKey = DigestUtils.md5DigestAsHex(this.config.getPath().toFile().getAbsolutePath().getBytes());
		initNettyClient();
	}


	public void initNettyClient() {
		String availableUrl = ZkRegister.getInstance().getAvailableUrl();
		if (StringUtils.isBlank(availableUrl)) {
			log.error("cl-log-service未启动，请先启动服务端！");
			System.exit(0);
		}
		client = new NettyClient(nettyClientId, availableUrl.split(":")[0], Integer.parseInt(availableUrl.split(":")[1]));
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> client.start());
	}

	@Override
	public void run() {
		do {
			DateUtils.sleep(3);
		} while (!ChannelHandlerContextHolder.registerSuccess(this.nettyClientId));

		historyLogProcess();
		Path curPath = config.getPath();
		log.warn("历史日志处理完成{}", curPath.toAbsolutePath().toString());
		FileAlterationObserver observer = new FileAlterationObserver(
				curPath.getParent().toFile(),
				FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".log")),
				null
		);
		File curFile = curPath.toFile();
		observer.addListener(new FileListener((file) -> {
			if (file.getName().equals(curFile.getName())) {
				parseFile(file);
			}
		}));
		try {
			new FileAlterationMonitor(TimeUnit.MILLISECONDS.toMillis(500), observer).start();
			log.warn("文件监听器启动成功！{} {}", curFile.getAbsolutePath(), curFile.getName());
		} catch (Exception e) {
			throw new RuntimeException("文件监控程序启动异常！", e);
		}
	}

	/**
	 * 历史日志处理。
	 *
	 * <pre>
	 *     此处有一个问题：
	 *         程序启动时会先处理历史文件夹中的历史日志，每处理一个文件，会将文件名记录到 _logRecord.txt 文件中。
	 *         当历史文件夹中的历史文件处理完成后，到第二天历史文件夹中会多一个当天的文件，如果这个时候日志收集进程重启，
	 *         程序会重新处理历史文件夹中的历史文件，此时如何判断这个文件已被处理？
	 *
	 *     比如：历史文件夹中有3个历史文件 history_1、history_2、history_3，程序启动后会处理这三个文件，处理完成后会将这3个文件名写到 _logRecord.txt 文件中。
	 *     程序运行到第二天，历史文件夹中 多了一个 history_4，此时程序挂了，进行重启操作，程序会再次处理历史文件夹中的历史日志，
	 *     前3个已经被记录下来，最后一个因为没记录，所以会被处理。但是 最后一个文件在前一天已经被实时处理过的，再处理一次会造成数据重复，如何解决？
	 * </pre>
	 */
	private void historyLogProcess() {
		Path curPath = config.getPath();
		File curFolder = curPath.getParent().toFile();
		// 获取上传记录
		File logRecordFile = Paths.get(curFolder.getAbsolutePath(), LOG_RECORD_FILE).toFile();
		if (!logRecordFile.exists()) {
			LogFactoryUtils.touchLogRecordFile(logRecordFile);
		}

		String[] historyFileNames = Paths.get(curFolder.getAbsolutePath(), "history").toFile().list();
		if (null == historyFileNames || historyFileNames.length < 1) {
			// 无历史日志文件
			return;
		}
		List<String> extractedLogFileNames = LogFactoryUtils.readLogRecordFile2Lines(logRecordFile);
		for (String historyFileName : historyFileNames) {
			// 已处理
			if (extractedLogFileNames.contains(historyFileName)) {
				continue;
			}
			// 处理未处理的文件
			// 一次性读完整个文件
			Path path = Paths.get(curFolder.getAbsolutePath(), "history", historyFileName);
			String curCacheKey = DigestUtils.md5DigestAsHex(path.toFile().getAbsolutePath().getBytes());
			Long lineNo = LineNoCacheRefreshJob.getLineNo(curCacheKey); // 这里的 cacheKey 应该使用当前文件的 cacheKey
			List<LogFactory.Log> logs;
			try (Stream<String> linesStream = Files.lines(path)) {
				List<String> lines = linesStream.skip(lineNo).collect(Collectors.toList());
				if (CollectionUtils.isEmpty(lines)) {
					continue;
				}
				logs = extractor.extract(lines);
				lineNo += lines.size();
			} catch (Exception e) {
				throw new RuntimeException("文件解析异常！", e);
			}
			ChannelHandlerContext channelHandlerContext = ChannelHandlerContextHolder.getChannelHandlerContext(nettyClientId);
			Channel channel = channelHandlerContext.channel();
			logs.stream().filter(Objects::nonNull).forEach(channel::writeAndFlush);
			log.info("日志已发送，总量：{}", logs.size());
			LineNoCacheRefreshJob.refresh(curCacheKey, lineNo);
			LogFactoryUtils.appendExtracted2LogRecordFile(logRecordFile, historyFileName);
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
		log.info("监听到文件变化，开始解析。{}", file.getAbsolutePath());
		long lineNo = LineNoCacheRefreshJob.getLineNo(cacheKey);
		log.info("当前已读取到{}行。", lineNo);
		List<LogFactory.Log> logs;
		Path path = file.toPath();
		try (Stream<String> linesStream = Files.lines(path)) {
			long count = linesStream.count();
			if (count < lineNo) {
				// 本次的文件内容行数 少于 已读取到的行号，说明文件内容被重置，要从头开始读取。
				// TODO 同时 更新历史文件记录，防止下次重启时读取重复的内容。
				lineNo = 0L;
				LineNoCacheRefreshJob.resetLineNo(cacheKey);
			}
			List<String> lines = Files.lines(path).skip(lineNo).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(lines)) {
				return;
			}
			logs = extractor.extract(lines);
			lineNo += lines.size();
			LineNoCacheRefreshJob.refresh(cacheKey, lineNo);
		} catch (Exception e) {
			throw new RuntimeException("文件解析异常！", e);
		}
		if (CollectionUtils.isEmpty(logs)) {
			log.warn("未读取到文件内容。{}", file.getAbsolutePath());
			return;
		}
		ChannelHandlerContext channelHandlerContext = ChannelHandlerContextHolder.getChannelHandlerContext(nettyClientId);
		Channel channel = channelHandlerContext.channel();
		logs.stream().filter(Objects::nonNull).forEach(channel::writeAndFlush);
		log.info("日志已发送，总量：{}", logs.size());
	}
}
