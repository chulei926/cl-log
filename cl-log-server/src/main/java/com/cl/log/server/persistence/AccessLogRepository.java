package com.cl.log.server.persistence;

import com.cl.log.config.common.PersistenceException;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.EsIndex;
import com.google.common.collect.Lists;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.recycler.Recycler;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Repository
public class AccessLogRepository extends AbstractRepository<AccessLog> {

	@Resource
	private RestHighLevelClient client;

	private static final int COUNT = 1000;

	@Override
	public void batchInsert(EsIndex index, List<AccessLog> list) {
		if (CollectionUtils.isEmpty(list)){
			return;
		}
		checkIndex(index);
		if (list.size() <= COUNT){
			insert(index, list);
			return;
		}
		List<List<AccessLog>> partition = Lists.partition(list, COUNT);
		for (List<AccessLog> logs : partition) {
			insert(index, logs);
		}
	}

	private void insert(EsIndex index, List<AccessLog> list){
		BulkRequest request = new BulkRequest(index.getName());
		for (AccessLog log : list) {
			request.add(new IndexRequest(index.getName()).id(UUID.randomUUID().toString()).source(log.convert()));
		}
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		BulkResponse responses;
		try {
			responses = client.bulk(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new PersistenceException("AccessLog插入到Elasticsearch异常!", e);
		}
		if (responses.hasFailures()) {
			throw new PersistenceException("AccessLog插入到Elasticsearch异常!" + responses.buildFailureMessage());
		}
	}



}
