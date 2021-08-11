package com.cl.log.agent.config;

import com.cl.log.config.common.SpringContextWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

	@Bean
	public SpringContextWrapper springContextUtils() {
		return new SpringContextWrapper();
	}

}
