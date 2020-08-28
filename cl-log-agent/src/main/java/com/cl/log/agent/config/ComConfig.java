package com.cl.log.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComConfig {

	@Bean
	public SpringContextUtils springContextUtils() {
		return new SpringContextUtils();
	}

}
