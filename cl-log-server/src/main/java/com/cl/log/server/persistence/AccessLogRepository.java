package com.cl.log.server.persistence;

import com.cl.log.config.common.PersistenceException;
import com.cl.log.server.model.AccessLog;
import com.cl.log.server.model.BizLog;
import com.cl.log.server.model.EsIndex;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Repository
public class AccessLogRepository extends AbstractRepository<AccessLog> {

	@Resource
	private RestHighLevelClient client;

	@Override
	public void batchInsert(EsIndex index, List<AccessLog> list) {
		BulkRequest request = new BulkRequest();
		for (AccessLog log : list) {
			request.add(new IndexRequest(index.getName()).id(UUID.randomUUID().toString()).source(log.convert()));
		}
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		try {
			client.bulk(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			throw new PersistenceException("BizLog插入到Elasticsearch异常!", e);
		}

	}


}
