package com.cl.log.server;

import com.cl.log.server.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClLogServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClLogServerApplication.class, args);
		NettyServer server = new NettyServer(12345);
		server.start();
	}

}
