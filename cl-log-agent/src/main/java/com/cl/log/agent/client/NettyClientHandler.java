package com.cl.log.agent.client;

import com.cl.log.config.model.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * 客户端消息处理.
 *
 * @author leichu 2020-08-17.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private final LogFactory.Log log;

	public NettyClientHandler(LogFactory.Log log) {
		this.log = log;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().writeAndFlush(this.log);
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