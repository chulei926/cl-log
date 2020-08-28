package com.cl.log.agent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ClLogAgentApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void fileWatchTest() throws Exception {
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path path = Paths.get("E:\\logs");
//		WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		WatchKey key;
		while ((key = watchService.take()) != null) {
//			System.out.println(key.pollEvents().size());
			key.pollEvents().stream().distinct().forEach(event -> {
				Path context = (Path) event.context();
				System.out.println("Event kind:" + event.kind() + ". File affected: " + context + "." + event.count());
				System.out.println(context.getFileName());
			});
//			key.pollEvents().iterator().forEachRemaining(event -> {
//				Path context = (Path) event.context();
//				System.out.println("Event kind:" + event.kind()+ ". File affected: " + context + "." + event.count());
//				System.out.println(context.getFileName());
//			});
			key.reset();
		}

	}

	@Test
	public void readFileTest() throws Exception {
		FileUtils.readLines(new File(""), StandardCharsets.UTF_8);
	}

	@Test
	public void monitor() throws Exception {


	}



}
