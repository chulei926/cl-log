package com.cl.log.server.server;

import com.cl.log.config.model.LogFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 服务端消息处理.
 *
 * @author leichu 2020-06-23.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<LogFactory.Log> {

	private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LogFactory.Log msg) throws Exception {
		ctx.channel().eventLoop().execute(new TaskDistributor(msg));
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(Unpooled.copiedBuffer("hello client", StandardCharsets.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("NettyServerHandler异常！", cause);
		ctx.close();
	}
}