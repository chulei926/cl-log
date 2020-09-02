package com.cl.log.agent.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty 客户端.
 *
 * @author leichu 2020-08-17.
 */
public class NettyClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private final String host;
	private final String ip;
	private final int port;
	private final String id;

	public NettyClient(String id, String ip, int port) {
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.host = String.format("%s:%s", ip, port);
	}

	public void start() {
		final NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			final Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							final ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
							pipeline.addLast("encoder", new ProtobufEncoder());
							pipeline.addLast(new NettyClientHandler(id));
							pipeline.fireChannelInactive();
						}
					});
			final ChannelFuture channelFuture = bootstrap.connect(this.ip, this.port).sync();
			logger.info("Netty客户端连接服务器[{}] 启动 成功", this.host);
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("Netty客户端连接服务器[{}] 启动 失败", this.host, e);
		} finally {
			group.shutdownGracefully();
		}
	}
}
