package com.cl.log.agent;

import com.cl.log.agent.file.FileListener;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FileMonitor {

	private static final Logger log = LoggerFactory.getLogger(FileMonitor.class);

	public static void main(String[] args) throws Exception {
		// 监控目录
		String rootDir = "E:\\logs";
		// 轮询间隔 5 秒
		long interval = TimeUnit.SECONDS.toMillis(1);
		// 创建一个文件观察器用于处理文件的格式
//		FileAlterationObserver _observer = new FileAlterationObserver(
//				);
		FileAlterationObserver observer = new FileAlterationObserver(rootDir,
				FileFilterUtils.and(
						FileFilterUtils.fileFileFilter(),
						FileFilterUtils.suffixFileFilter(".log")),  //过滤文件格式
				null);

		observer.addListener(new FileListener((file) -> {
			System.out.println("变化");
		})); //设置文件变化监听器
		//创建文件变化监听器
		FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
		// 开始监控
		monitor.start();
		log.warn("启动成功");
	}


}
