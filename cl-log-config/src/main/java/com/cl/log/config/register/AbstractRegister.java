package com.cl.log.config.register;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * 注册中心抽象类.
 *
 * @author leichu 2020-06-23.
 */
public abstract class AbstractRegister implements Register {

	private int reqCount = 0;

	/**
	 * 注册表.
	 */
	private static final Map<String, Object> registerTable = Maps.newConcurrentMap();

	public void register(String key, Object value) {
		registerTable.put(key, value);
	}

	public void unRegister(String key) {
		registerTable.remove(key);
	}

	public List<String> getUrls() {
		return Lists.newArrayList(registerTable.keySet());
	}

	public int serverCount() {
		throw new AbstractMethodError("方法未实现");
	}

	public int reqCount() {
		return reqCount;
	}

	public void addReqCount() {
		this.reqCount++;
	}

	public void resetReqCount() {
		this.reqCount = 0;
	}
}
