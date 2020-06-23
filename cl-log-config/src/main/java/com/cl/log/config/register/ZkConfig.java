package com.cl.log.config.register;

import java.util.ResourceBundle;

/**
 * zookeeper 配置信息.
 *
 * @author leichu 2020-06-23.
 */
public class ZkConfig {

	public static String HOST = null;
	public static int SESSION_TIMEOUT = 0;
	public static int CONNECTION_TIMEOUT = 0;

	static {
		ResourceBundle resource = ResourceBundle.getBundle("application");
		HOST = resource.getString("zookeeper.address");
		SESSION_TIMEOUT = Integer.parseInt(resource.getString("zookeeper.sessionTimeout"));
		CONNECTION_TIMEOUT = Integer.parseInt(resource.getString("zookeeper.connectionTimeout"));
	}

}
