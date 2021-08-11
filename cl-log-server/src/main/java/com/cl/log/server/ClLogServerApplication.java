package com.cl.log.server;

import com.cl.log.server.server.NettyServer;
import com.cl.log.server.task.RemainLogHandlerBeforeApplicationDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 服务端启动入口.
 * <p>
 * 添加 scanBasePackages 是因为该工程依赖的 cl-log-config 中使用了 @Configuration 注解。
 * </p>
 *
 * @author leichu 2021-07-06.
 */
@SpringBootApplication(scanBasePackages = "com.cl.log")
public class ClLogServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClLogServerApplication.class);

	private static ConfigurableApplicationContext applicationContext;

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

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(ClLogServerApplication.class, args);
		NettyServer server = new NettyServer(12345);
		server.start();
	}

}
