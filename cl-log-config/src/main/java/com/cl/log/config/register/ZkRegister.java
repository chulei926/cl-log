package com.cl.log.config.register;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * zookeeper 注册中心.
 *
 * @author leichu 2020-06-23.
 */
public class ZkRegister extends AbstractRegister {

	private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

	private static final String path = "log_register";
	private static volatile CuratorFramework client = null;
	private static ZkRegister INSTANCE = null;

	public static ZkRegister getInstance() {
		if (null == client) {
			synchronized (ZkRegister.class) {
				if (null == client) {
					INSTANCE = new ZkRegister();
				}
			}
		}
		return INSTANCE;
	}

	private ZkRegister() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder()
				.connectString(ZkConfig.HOST)
				.sessionTimeoutMs(ZkConfig.SESSION_TIMEOUT)
				.connectionTimeoutMs(ZkConfig.CONNECTION_TIMEOUT)
				.retryPolicy(retryPolicy)
				.namespace(path)
				.build();

		client.getConnectionStateListenable().addListener((client1, state) -> {
			if (state == ConnectionState.LOST) {
				logger.warn("zookeeper[{}] 连接断开。", ZkConfig.HOST);
			} else if (state == ConnectionState.CONNECTED) {
				logger.warn("zookeeper[{}] 连接成功。", ZkConfig.HOST);
			} else if (state == ConnectionState.RECONNECTED) {
				logger.warn("zookeeper[{}] 已连接。", ZkConfig.HOST);
			}
		});

		client.start();

		final PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/", true);
		final PathChildrenCacheListener childrenCacheListener = (client, event) -> {
			try {
				ChildData data = event.getData();
				switch (event.getType()) {
					case CHILD_ADDED:
						logger.warn("子节点增加, path={}, data={}", data.getPath(), new String(data.getData(), "UTF-8"));
						break;
					case CHILD_UPDATED:
						logger.warn("子节点更新, path={}, data={}", data.getPath(), new String(data.getData(), "UTF-8"));
						break;
					case CHILD_REMOVED:
						logger.warn("子节点删除, path={}, data={}", data.getPath(), new String(data.getData(), "UTF-8"));
						break;
					default:
						break;
				}

			} catch (UnsupportedEncodingException e) {
				logger.error("配置 zookeeper 节点发现监听器异常！", e);
			}
		};
		pathChildrenCache.getListenable().addListener(childrenCacheListener);
		try {
			pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
		} catch (Exception e) {
			logger.error(" zookeeper 节点发现监听器启动异常！", e);
		}
	}

	@Override
	public void register(String key, Object value) {
		super.register(key, value);
		try {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + key, key.getBytes());
			final byte[] bytes = client.getData().forPath("/" + key);
			logger.warn("服务注册成功 {}", new String(bytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
