package com.cl.log.agent.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * 客户端消息处理.
 *
 * @author leichu 2020-08-17.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private String id;

	public NettyClientHandler(String id) {
		this.id = id;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelHandlerContextHolder.register(id, ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		ctx.channel().writeAndFlush(log);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		System.out.println("服务器回复的消息：" + byteBuf.toString(StandardCharsets.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
