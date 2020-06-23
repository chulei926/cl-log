package com.cl.log.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

	@Bean
	public SpringContextUtil springApplicationContextUtil() {
		return new SpringContextUtil();
	}
}
