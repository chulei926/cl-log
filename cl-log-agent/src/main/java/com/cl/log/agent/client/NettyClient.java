package com.cl.log.agent.client;

import com.cl.log.config.model.LogFactory;
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

import java.util.concurrent.BlockingQueue;

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

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.key = String.format("%s:%s", this.host, this.port);
	}


	public void start(LogFactory.Log log) {
		final NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			final Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							final ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast("encoder", new ProtobufEncoder());
							pipeline.addLast(new NettyClientHandler(log));
						}
					});
			final ChannelFuture channelFuture = bootstrap.connect(this.host, this.port).sync();
			logger.info("客户端 启动 成功");
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
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
