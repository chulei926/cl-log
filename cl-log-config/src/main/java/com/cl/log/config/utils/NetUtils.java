package com.cl.log.config.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class NetUtils {

	public static String getIp() {
		List<String> ips = Lists.newArrayList();
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();
				if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
						ips.add(address.getHostAddress());
					}
				}
				// 没有找到 SiteLocalAddress，降低条件，找所有的 IPv4
				if (ips.size() < 1) {
					while (addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						if (address instanceof Inet4Address) {
							ips.add(address.getHostAddress());
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("IP地址获取失败", e);
		}
		return ips.size() < 1 ? "" : Joiner.on(",").join(ips);
	}
}

