package com.cl.log.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.Objects;

@Configuration
public class ZkLockConfig {

	@Resource
	private Environment environment;

	/**
	 * 创建 CuratorFramework 对象并连接 Zookeeper
	 *
	 * @return CuratorFramework
	 */
	@Bean(initMethod = "start")
	public CuratorFramework curatorFramework() {
		return CuratorFrameworkFactory.newClient(
				environment.getProperty("zookeeper.address"),
				Integer.parseInt(Objects.requireNonNull(environment.getProperty("zookeeper.sessionTimeout"))),
				Integer.parseInt(Objects.requireNonNull(environment.getProperty("zookeeper.connectionTimeout"))),
				new RetryNTimes(
						Integer.parseInt(Objects.requireNonNull(environment.getProperty("zookeeper.retryCount"))),
						Integer.parseInt(Objects.requireNonNull(environment.getProperty("zookeeper.elapsedTimeMs")))
				)
		);
	}
}
