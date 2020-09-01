package com.cl.log.agent.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty 客户端.
 *
 * @author leichu 2020-08-17.
 */
public class NettyClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private String host;
	private int port;
	private String key;
	private String id;

	public NettyClient(String id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.key = String.format("%s:%s", this.host, this.port);
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
							pipeline.addLast("encoder", new ProtobufEncoder());
							pipeline.addLast(new NettyClientHandler(id));
							pipeline.fireChannelInactive();
						}
					});
			final ChannelFuture channelFuture = bootstrap.connect(this.host, this.port).sync();
			logger.info("客户端 启动 成功");
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("客户端 启动 失败", e);
		} finally {
			group.shutdownGracefully();
		}


	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
