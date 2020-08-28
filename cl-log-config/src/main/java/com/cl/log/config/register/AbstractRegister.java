package com.cl.log.config.register;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 注册中心抽象类.
 *
 * @author leichu 2020-06-23.
 */
public abstract class AbstractRegister implements Register {

	/**
	 * 注册表.
	 */
	public Map<String, Object> registerTable = Maps.newConcurrentMap();
	public Map<String, Long> balanceMap = Maps.newConcurrentMap();

	public void register(String key, Object value) {
		registerTable.put(key, value);
	}

	public void unRegister(String key) {
		registerTable.remove(key);
	}

	public int count() {
		return registerTable.size();
	}
}
