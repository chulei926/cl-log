package com.cl.log.config.register;

/**
 * 注册中心.
 *
 * @author leichu 2020-06-23.
 */
public interface Register {

	/**
	 * 注册.
	 *
	 * @param key
	 * @param value
	 */
	void register(String key, Object value);

	/**
	 * 注销.
	 *
	 * @param key
	 */
	void unRegister(String key);

	/**
	 * 统计注册表的数量.
	 *
	 * @return
	 */
	int count();
}
