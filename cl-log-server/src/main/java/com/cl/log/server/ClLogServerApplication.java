package com.cl.log.server;

import com.cl.log.config.utils.NetUtils;
import com.cl.log.server.server.NettyServer;
import io.netty.util.NetUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClLogServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClLogServerApplication.class, args);
		NettyServer server = new NettyServer(NetUtils.getIp(), 8889);
		server.start();
	}

}
