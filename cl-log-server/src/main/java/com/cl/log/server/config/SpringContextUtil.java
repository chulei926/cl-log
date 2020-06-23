package com.cl.log.server.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring上下文工具类.
 *
 * @author leichu 2019-03-12.
 */
public class SpringContextUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtil.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}


	public static <T> T getBean(String name) throws BeansException {
		return (T) applicationContext.getBean(name);
	}

	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return (T) applicationContext.getBean(clazz);
	}
}
