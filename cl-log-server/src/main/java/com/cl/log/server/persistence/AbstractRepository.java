package com.cl.log.server.persistence;

import com.cl.log.server.model.EsIndex;

public abstract class AbstractRepository<T> implements IRepository<T> {

	/**
	 * 创建索引.
	 * <pre>
	 *     1. 创建 索引
	 *     2. 创建 mapping
	 * </pre>
	 * @param index
	 */
	public void createIndex(EsIndex index) {
	}

}
