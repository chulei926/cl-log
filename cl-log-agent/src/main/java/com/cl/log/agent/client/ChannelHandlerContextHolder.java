package com.cl.log.agent.client;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

public class ChannelHandlerContextHolder {

	private static final Map<String, ChannelHandlerContext> map = Maps.newConcurrentMap();

	public static void register(String key, ChannelHandlerContext ctx){
		map.put(key, ctx);
	}

	public static void unRegister(String key){
		map.remove(key);
	}

	public static ChannelHandlerContext getChannelHandlerContext(String key){
		return map.get(key);
	}

	public static boolean registerSuccess(String key){
		return map.containsKey(key);
	}

}
