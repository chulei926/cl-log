package com.cl.log.server.persistence;

import com.cl.log.server.config.SpringContextUtil;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.BizLog;
import com.cl.log.server.model.EsIndex;
import com.cl.log.server.model.PerfLog;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRepository<T> implements IRepository<T> {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

	protected static Set<String> indexNameRepositoryCache = Sets.newConcurrentHashSet();

	protected static Map<String, ClassPathResource> mappingPathResourceMap = Maps.newConcurrentMap();

	static {
		mappingPathResourceMap.clear();
		mappingPathResourceMap.put(BizLog.INDEX_PREFIX, new ClassPathResource("mapping/mapping_biz.xml"));
		mappingPathResourceMap.put(PerfLog.INDEX_PREFIX, new ClassPathResource("mapping/mapping_perf.xml"));
		mappingPathResourceMap.put(AccessLog.INDEX_PREFIX, new ClassPathResource("mapping/mapping_access.xml"));
	}

	public void checkIndex(EsIndex index) {
		Assert.notNull(index, "入参index不能为空！");
		Assert.notNull(index.getName(), "入参index.name不能为空！");
		String indexName = index.getName();
		// 1. 先判断缓存中是否存在
		if (indexNameRepositoryCache.contains(indexName)) {
			return;
		}
		InterProcessMutex interProcessMutex = null;
		try {
			// 创建锁对象
			interProcessMutex = new InterProcessMutex(SpringContextUtil.getBean(CuratorFramework.class), "/cl-log-lock/" + indexName);
			// 获取锁
			interProcessMutex.acquire(10, TimeUnit.SECONDS);
			// 如果获取锁成功，则执行对应逻辑
			logger.warn("获取到锁！{}", indexName);
			// 2. 缓存中没有，调用ES服务判断
			IndexRepository indexRepository = SpringContextUtil.getBean(IndexRepository.class);
			if (indexRepository.exist(indexName)) {
				// ES 中存在，放入缓存
				indexNameRepositoryCache.add(indexName);
				return;
			}
			// 3. 缓存 和 ES 中都没有，主动创建
			ClassPathResource resource = mappingPathResourceMap.get(indexName.substring(0, indexName.indexOf("-") + 1));
			File tmp = Paths.get(FileUtils.getUserDirectoryPath(), UUID.randomUUID().toString() + ".xml").toFile();
			try (InputStream is = resource.getInputStream();
			     OutputStream os = new FileOutputStream(tmp)) {
				IOUtils.copyLarge(is, os);
			} catch (IOException e) {
				throw new RuntimeException("ES mapping 文件加载失败！" + indexName, e);
			}
			EsIndex fullIndex = EsIndex.xml2Index(tmp);
			fullIndex.setName(indexName);
			indexRepository.create(fullIndex);
			// 4. 创建完成，重新放入缓存
			indexNameRepositoryCache.add(indexName);
			FileUtils.deleteQuietly(tmp);
			logger.warn("索引创建成功！{}", indexName);
		} catch (Exception e) {
			logger.error("checkIndex异常！", e);
		} finally {
			// 释放锁
			if (null != interProcessMutex) {
				try {
					interProcessMutex.release();
				} catch (Exception e) {
					logger.error("ZK分布式锁释放异常！", e);
				}
			}
			logger.warn("释放锁！{}", indexName);
		}
	}

}
