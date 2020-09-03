package com.cl.log.agent.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 客户端消息处理.
 *
 * @author leichu 2020-08-17.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

	private final String id;

	public NettyClientHandler(String id) {
		this.id = id;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 接收到客户端消息
		ChannelHandlerContextHolder.register(id, ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		logger.info("收到服务端响应{}", byteBuf.toString(StandardCharsets.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
