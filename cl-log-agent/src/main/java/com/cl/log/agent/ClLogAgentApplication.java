package com.cl.log.agent;

import com.cl.log.agent.config.LogFileConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableAsync
public class ClLogAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClLogAgentApplication.class, args);
		// 采用多线程的方式，启动 收集任务，有多少个需要收集的日志，就起多少个线程
		int count = LogFileConfig.getLogFileCount();
		ExecutorService executorService = Executors.newFixedThreadPool(count);
		LogFileConfig.getConfigs().forEach(config-> executorService.execute(new LogTask(config)));
	}

}
