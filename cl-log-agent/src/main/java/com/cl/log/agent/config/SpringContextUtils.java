package com.cl.log.agent.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring上下文工具类.
 *
 * @author leichu 2019-03-12.
 */
public class SpringContextUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext; // Spring应用上下文环境

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtils.applicationContext = applicationContext;
	}

	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return (T) applicationContext.getBean(clazz);
	}
}
