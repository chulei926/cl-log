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
	 * @param key key.
	 * @param value value.
	 */
	void register(String key, Object value);

	/**
	 * 注销.
	 *
	 * @param key key.
	 */
	void unRegister(String key);

	/**
	 * 统计注册表的数量.
	 *
	 * @return cnt.
	 */
	int count();

	/**
	 * 从注册中心中获取一个 可用的 url 地址.
	 *
	 * @return url.
	 */
	String getAvailableUrl();
}
