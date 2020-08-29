package com.cl.log.config.register;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

/**
 * zookeeper 注册中心.
 *
 * @author leichu 2020-06-23.
 */
public class ZkRegister extends AbstractRegister {

	private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

	private static final String path = "cl-log";
	private static volatile CuratorFramework client = null;
	private static ZkRegister INSTANCE = null;

	private static final String SERVER_PREFIX = "/server/";
	private static final String CONFIG_PREFIX = "/config/";

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
			switch (state) {
				case LOST:
					logger.info("zookeeper[{}] 连接断开。", ZkConfig.HOST);
					break;
				case CONNECTED:
					logger.info("zookeeper[{}] 连接成功。", ZkConfig.HOST);
					break;
				case RECONNECTED:
					logger.info("zookeeper[{}] 已连接。", ZkConfig.HOST);
					break;
				default:
					break;
			}
		});

		client.start();

		final PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/server", true);
		final PathChildrenCacheListener childrenCacheListener = (client, event) -> {
			ChildData data = event.getData();
			String key = data.getPath().replace(SERVER_PREFIX, "");
			Object value = byte2Obj(data.getData());
			switch (event.getType()) {
				case CHILD_ADDED:
					logger.info("子节点增加, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
					resetReqCount();  // 重置
					super.register(key, value); // 缓存
					break;
				case CHILD_UPDATED:
					logger.info("子节点更新, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
					resetReqCount();  // 重置
					super.register(key, value); // 缓存
					// 先删除
					try {
						client.delete().forPath(SERVER_PREFIX + key);
					} catch (Exception e) {
						throw new RuntimeException("服务注册异常！", e);
					}
					// 再注册
					this.register(key, value);
					break;
				case CHILD_REMOVED:
					logger.info("子节点删除, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
					resetReqCount();  // 重置
					super.unRegister(key);  // 清除缓存
					break;
				default:
					break;
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
		try {
			// 增加 zk 配置.
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(SERVER_PREFIX + key, obj2Byte(value));
			logger.warn("服务注册成功 {} - {}", key, value);
		} catch (Exception e) {
			throw new RuntimeException("服务注册异常！", e);
		}
	}

	/**
	 * 获取一个可用的 URL 地址.
	 *
	 * @return URL.
	 */
	public String getAvailableUrl() {
		List<String> urls = alreadyRegisterUrl();
		if (urls.size() < 1) {
			return null;
		}
		if (urls.size() == 1) {
			return urls.get(0);
		}
		int mod = reqCount() % urls.size();
		String url = urls.get(mod);
		addReqCount();
		return url;
	}

	/**
	 * 获取已经注册的 url。
	 * <pre>
	 *     先从缓存获取，缓存获取不到再从 zk 获取.
	 * </pre>
	 *
	 * @return url 列表.
	 */
	private List<String> alreadyRegisterUrl() {
		// 1. 从缓存获取.
		List<String> urls = super.getUrls();
		if (urls.size() > 0) {
			return urls;
		}
		// 2. 从注册中心中获取已经注册上来的所有 url
		Map<String, Object> urlsMap = Maps.newHashMap();
		try {
			List<String> keys = client.getChildren().forPath("/server");
			if (null == keys || keys.size() < 1) {
				return Lists.newArrayList();
			}
			for (String key : keys) {
				final byte[] bytes = client.getData().forPath(SERVER_PREFIX + key);
				Object url = byte2Obj(bytes);
				if (null != url) {
					urlsMap.put(key, url);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("获取已注册URL列表异常！", e);
		}
		if (urlsMap.size() > 0) {
			// 放入缓存
			urlsMap.forEach(this::register);
		}
		return Lists.newArrayList(urlsMap.keySet());
	}

	/**
	 * 设置值.
	 *
	 * @param key   key.
	 * @param value value.
	 */
	public void set(String key, Object value) {
		try {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(CONFIG_PREFIX + key, obj2Byte(value));
		} catch (Exception e) {
			throw new RuntimeException("设置值异常！", e);
		}
	}

	/**
	 * 获取值.
	 *
	 * @param key key.
	 * @return value.
	 */
	public Object get(String key) {
		try {
			final byte[] bytes = client.getData().forPath(CONFIG_PREFIX + key);
			return byte2Obj(bytes);
		} catch (Exception e) {
			throw new RuntimeException("获取值异常！", e);
		}
	}

	/**
	 * 序列化.
	 *
	 * @param obj 对象.
	 * @return byte数组.
	 */
	private byte[] obj2Byte(Object obj) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(bos);) {
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("对象序列化异常！", e);
		}
	}

	/**
	 * 反序列化.
	 *
	 * @param bytes byte数组.
	 * @return 对象.
	 */
	private Object byte2Obj(byte[] bytes) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		     ObjectInputStream ois = new ObjectInputStream(bis);) {
			return ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException("对象反序列化异常！", e);
		}
	}

}
