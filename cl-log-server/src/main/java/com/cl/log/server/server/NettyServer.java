package com.cl.log.server.server;

import com.cl.log.config.model.LogFactory;
import com.cl.log.config.register.ZkRegister;
import com.cl.log.config.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private final String host;
	private final int port;

	private NioEventLoopGroup boss;
	private NioEventLoopGroup worker;

	public NettyServer(int port) {
		this.port = port;
		String ip = NetUtils.getIp();
		if (ip.contains(",")) {
			ip = ip.substring(0, ip.indexOf(","));
		}
		this.host = String.format("%s:%s", ip, port);
	}

	public void start() {
		registerHook();
		boss = new NioEventLoopGroup(1);
		worker = new NioEventLoopGroup();
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
						pipeline.addLast(new ProtobufVarint32FrameDecoder());
						pipeline.addLast("decoder", new ProtobufDecoder(LogFactory.Log.getDefaultInstance()));
						pipeline.addLast(new NettyServerHandler());

					}
				}); // 创建一个通道初始化对象
		try {
			// 启动
			ChannelFuture channelFuture = bootstrap.bind(this.port).sync();
			logger.info("Netty服务器[{}] 启动 成功", this.host);
			// 注册服务
			ZkRegister.getInstance().register(this.host, this.host);
			channelFuture.channel().closeFuture().sync();
			// 从注册中心 下线
			ZkRegister.getInstance().unRegister(this.host);
			logger.info("Netty服务器[{}] 正在关闭", this.host);
		} catch (Exception e) {
			logger.error("Netty服务器[{}] 异常!", this.host, e);
			// 从注册中心 下线
			ZkRegister.getInstance().unRegister(this.host);
		}
	}

	private void registerHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(() -> {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
			logger.warn("Netty服务端[{}]  boss worker 均已关闭", host);
		}));
	}

}
