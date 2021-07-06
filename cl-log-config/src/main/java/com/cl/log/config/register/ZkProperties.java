package com.cl.log.config.register;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * zookeeper 配置信息.
 *
 * @author leichu 2020-06-23.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zookeeper")
public class ZkProperties {

	private String address;
	private Integer sessionTimeout;
	private Integer connectionTimeout;
	private Integer retryCount;
	private Integer elapsedTimeMs;

}
