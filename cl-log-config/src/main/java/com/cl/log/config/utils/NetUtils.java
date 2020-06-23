package com.cl.log.config.utils;

import java.net.InetAddress;

public class NetUtils {

	public static String getIp() {
		String ip = null;
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			ip = inetAddress.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ip;
	}
}

