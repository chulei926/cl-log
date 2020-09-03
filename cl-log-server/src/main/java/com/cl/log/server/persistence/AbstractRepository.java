package com.cl.log.server.persistence;

import com.cl.log.server.config.SpringContextUtil;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.BizLog;
import com.cl.log.server.model.EsIndex;
import com.cl.log.server.model.PerfLog;
import com.google.common.collect.Sets;
import org.springframework.util.Assert;

import java.util.Set;

public abstract class AbstractRepository<T> implements IRepository<T> {

	protected static Set<String> indexNameRepositoryCache = Sets.newConcurrentHashSet();

	public void checkIndex(EsIndex index) {
		Assert.notNull(index, "入参index不能为空！");
		Assert.notNull(index.getName(), "入参index.name不能为空！");
		String indexName = index.getName();
		// 1. 先判断缓存中是否存在
		if (indexNameRepositoryCache.contains(indexName)){
			return;
		}
		// 2. 缓存中没有，调用ES服务判断
		IndexRepository indexRepository = SpringContextUtil.getBean(IndexRepository.class);
		if (indexRepository.exist(indexName)){
			// ES 中存在，放入缓存
			indexNameRepositoryCache.add(indexName);
			return;
		}
		// 3. 缓存 和 ES 中都没有，主动创建
		String mappingPath = null;
		String path = AbstractRepository.class.getResource("/").getPath();
		path = path.startsWith("/") ? path.substring(1) : path;
		if (indexName.startsWith(BizLog.INDEX_PREFIX)){
			mappingPath = path + "mapping_biz.xml";
		} else if (indexName.startsWith(PerfLog.INDEX_PREFIX)){
			mappingPath = path + "mapping_perf.xml";
		} else if (indexName.startsWith(AccessLog.INDEX_PREFIX)){
			mappingPath = path + "mapping_access.xml";
		}  // DO NOTHING
		EsIndex fullIndex = EsIndex.xml2Index(mappingPath);
		fullIndex.setName(indexName);
		indexRepository.create(fullIndex);
		// 4. 创建完成，重新放入缓存
		indexNameRepositoryCache.add(indexName);
	}

}
