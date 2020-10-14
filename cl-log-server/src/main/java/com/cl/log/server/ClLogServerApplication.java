package com.cl.log.server;

import com.cl.log.server.server.NettyServer;
import com.cl.log.server.task.RemainLogHandlerBeforeApplicationDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ClLogServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClLogServerApplication.class);

	private static ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(ClLogServerApplication.class, args);
		NettyServer server = new NettyServer(12345);
		server.start();
	}

	static {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(() -> {
			if (applicationContext != null) {
				logger.info("正在关闭程序释放资源，请稍等....");
				RemainLogHandlerBeforeApplicationDestroy handler = applicationContext.getBean(RemainLogHandlerBeforeApplicationDestroy.class);
				handler.handle();
				applicationContext.close();
				logger.warn("cl-log服务端程序已关闭！");
			}
		}));
	}

}
