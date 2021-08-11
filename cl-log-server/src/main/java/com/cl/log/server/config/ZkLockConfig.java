package com.cl.log.server.config;

import com.cl.log.config.register.ZkProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class ZkLockConfig {

	@Resource
	private ZkProperties zkProperties;

	/**
	 * 创建 CuratorFramework 对象并连接 Zookeeper
	 *
	 * @return CuratorFramework
	 */
	@Bean(initMethod = "start")
	public CuratorFramework curatorFramework() {
		return CuratorFrameworkFactory.newClient(zkProperties.getAddress(), zkProperties.getSessionTimeout(), zkProperties.getConnectionTimeout(),
				new RetryNTimes(zkProperties.getRetryCount(), zkProperties.getElapsedTimeMs())
		);
	}


}
