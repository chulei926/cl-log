package com.cl.log.server.server;

import com.cl.log.config.model.LogFactory;
import com.cl.log.config.register.ZkRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private String host;
	private int port;
	private String key;

	public NettyServer(String host, int port) {
		this.host = host;
		this.port = port;
		this.key = String.format("%s:%s", this.host, this.port);
	}

	public void start() {
		NioEventLoopGroup boss = new NioEventLoopGroup(1);
		NioEventLoopGroup worker = new NioEventLoopGroup();
		// 创建 启动引导
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss, worker) // 设置线程组
				.channel(NioServerSocketChannel.class) // 设置使用的 channel
				.option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列等待连接的个数
				.childOption(ChannelOption.SO_KEEPALIVE, true) // 保持活动连接状态
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						final ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("decoder", new ProtobufDecoder(LogFactory.Log.getDefaultInstance()));
						pipeline.addLast(new NettyServerHandler());

					}
				}); // 创建一个通道初始化对象
		logger.info(">>>>> server " + host + ":" + port + " is ready <<<<<");
		try {
			// 启动
			ChannelFuture channelFuture = bootstrap.bind(port).sync();
			logger.info(">>>>> server " + host + ":" + port + " is ok <<<<<");
			// 注册服务
			ZkRegister.getInstance().register(this.key, this);
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("Netty服务器异常!", e);
			// 从注册中心 下线
			ZkRegister.getInstance().unRegister(this.key);
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
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

}
