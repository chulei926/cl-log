package com.cl.log.server.persistence;

import com.cl.log.server.config.SpringContextUtil;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.BizLog;
import com.cl.log.server.model.EsIndex;
import com.cl.log.server.model.PerfLog;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractRepository<T> implements IRepository<T> {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

	protected static Set<String> indexNameRepositoryCache = Sets.newConcurrentHashSet();

	public synchronized void checkIndex(EsIndex index) {
		Assert.notNull(index, "入参index不能为空！");
		Assert.notNull(index.getName(), "入参index.name不能为空！");
		String indexName = index.getName();
		// 1. 先判断缓存中是否存在
		if (indexNameRepositoryCache.contains(indexName)) {
			return;
		}
		// 2. 缓存中没有，调用ES服务判断
		IndexRepository indexRepository = SpringContextUtil.getBean(IndexRepository.class);
		if (indexRepository.exist(indexName)) {
			// ES 中存在，放入缓存
			indexNameRepositoryCache.add(indexName);
			return;
		}
		// 3. 缓存 和 ES 中都没有，主动创建
		String mappingPath = null;
		if (indexName.startsWith(BizLog.INDEX_PREFIX)) {
			mappingPath = "mapping/mapping_biz.xml";
		} else if (indexName.startsWith(PerfLog.INDEX_PREFIX)) {
			mappingPath = "mapping/mapping_perf.xml";
		} else if (indexName.startsWith(AccessLog.INDEX_PREFIX)) {
			mappingPath = "mapping/mapping_access.xml";
		} else {
			// DO NOTHING
			throw new IllegalArgumentException("不支持的索引类型！" + index.getName());
		}
		ClassPathResource resource = new ClassPathResource(mappingPath);
		File tmp = Paths.get(FileUtils.getUserDirectoryPath(), UUID.randomUUID().toString() + ".xml").toFile();
		try (InputStream is = resource.getInputStream();
		     OutputStream os = new FileOutputStream(tmp)) {
			IOUtils.copyLarge(is, os);
		} catch (IOException e) {
			throw new RuntimeException("ES mapping 文件加载失败！" + mappingPath, e);
		}
		EsIndex fullIndex = EsIndex.xml2Index(tmp);
		fullIndex.setName(indexName);
		indexRepository.create(fullIndex);
		// 4. 创建完成，重新放入缓存
		indexNameRepositoryCache.add(indexName);
		FileUtils.deleteQuietly(tmp);
	}

}
