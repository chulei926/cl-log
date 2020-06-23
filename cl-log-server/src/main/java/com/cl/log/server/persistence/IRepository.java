package com.cl.log.server.persistence;

import com.cl.log.server.model.EsIndex;

import java.util.List;

public interface IRepository<T> {

	/**
	 * 批量插入.
	 *
	 * @param index 索引.
	 * @param list  对象列表.
	 */
	void batchInsert(EsIndex index, List<T> list);

}
