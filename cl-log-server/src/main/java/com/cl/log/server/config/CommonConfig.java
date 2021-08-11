package com.cl.log.server.config;

import com.cl.log.config.common.SpringContextWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

	@Bean
	public SpringContextWrapper springApplicationContextUtil() {
		return new SpringContextWrapper();
	}
}
