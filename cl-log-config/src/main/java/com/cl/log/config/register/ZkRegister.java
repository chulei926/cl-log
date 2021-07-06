package com.cl.log.config.register;

import com.cl.log.config.common.SpringContextWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

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
@Slf4j
public class ZkRegister extends AbstractRegister {

	private static final String path = "cl-log";
	private static final String SERVER_PREFIX = "/server/";
	private static final String CONFIG_PREFIX = "/config/";
	private static volatile CuratorFramework client = null;
	private static ZkRegister INSTANCE = null;

	private ZkRegister() {
		final ZkProperties zkConfig = SpringContextWrapper.getBean(ZkProperties.class);
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder()
				.connectString(zkConfig.getAddress())
				.sessionTimeoutMs(zkConfig.getSessionTimeout())
				.connectionTimeoutMs(zkConfig.getConnectionTimeout())
				.retryPolicy(retryPolicy)
				.namespace(path)
				.build();

		client.getConnectionStateListenable().addListener((client1, state) -> {
			switch (state) {
				case LOST:
					log.info("zookeeper[{}] 连接断开。", zkConfig.getAddress());
					break;
				case CONNECTED:
					log.info("zookeeper[{}] 连接成功。", zkConfig.getAddress());
					break;
				case RECONNECTED:
					log.info("zookeeper[{}] 已连接。", zkConfig.getAddress());
					break;
				default:
					break;
			}
		});

		client.start();

		final PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/server", true);
		final PathChildrenCacheListener childrenCacheListener = (client, event) -> {
			ChildData data = event.getData();
			if (data == null) {
				return;
			}
			String key = data.getPath().replace(SERVER_PREFIX, "");
			Object value = byte2Obj(data.getData());
			switch (event.getType()) {
				case CHILD_ADDED:
					log.info("子节点增加, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
					resetReqCount();  // 重置
					super.register(key, value); // 缓存
					break;
				case CHILD_UPDATED:
					log.info("子节点更新, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
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
					log.info("子节点删除, path={}, data={}", data.getPath(), byte2Obj(data.getData()));
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
			log.error(" zookeeper 节点发现监听器启动异常！", e);
		}
	}

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

	@Override
	public void register(String key, Object value) {
		try {
			// 增加 zk 配置.
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(SERVER_PREFIX + key, obj2Byte(value));
			log.warn("服务注册成功 {} - {}", key, value);
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
			urlsMap.forEach(super::register);
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
		String path = CONFIG_PREFIX + key;
		try {
			//检测是否存在该路径。
			Stat stat = client.checkExists().forPath(path);
			if (null == stat) {
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, obj2Byte(value));
				return;
			}
			client.setData().forPath(path, obj2Byte(value));
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
		} catch (KeeperException.NoNodeException e) {
			return null; // 不存在当前节点
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
