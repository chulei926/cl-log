package com.cl.log.server.persistence;

import com.cl.log.config.common.ConvertException;
import com.cl.log.config.common.PersistenceException;
import com.cl.log.server.model.EsIndex;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class IndexRepository extends AbstractRepository<EsIndex> {

	private static final Logger logger = LoggerFactory.getLogger(IndexRepository.class);

	@Resource
	private Integer numberOfShards;
	@Resource
	private Integer numberOfReplicas;
	@Resource
	private Integer maxResultWindow;
	@Resource
	private RestHighLevelClient client;

	public boolean exist(String indexName){
		try {
			GetIndexRequest request = new GetIndexRequest(indexName);
			return client.indices().exists(request, RequestOptions.DEFAULT);
		} catch (Exception e){
			throw new PersistenceException(String.format("判断索引[%s]是否存在出现异常！",indexName), e);
		}
	}

	public void create(EsIndex index) {
		XContentBuilder builder;
		try {
			builder = EsIndex.buildMapping(index);
		} catch (Exception e) {
			throw new ConvertException(String.format("Elasticsearch Mapping 转换异常！%s", index.toString()), e);
		}
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(index.getName());
		createIndexRequest.settings(Settings.builder()
				.put("index.number_of_shards", numberOfShards)
				.put("index.number_of_replicas", numberOfReplicas)
				.put("index.max_result_window", maxResultWindow)
		);
		try {
			client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			throw new PersistenceException(String.format("索引[%s]创建出现异常！[%s]", index, builder.toString()), e);
		}

		PutMappingRequest putMappingRequest = new PutMappingRequest(index.getName());
		putMappingRequest.source(builder);
		AcknowledgedResponse putMappingResponse;
		try {
			putMappingResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			throw new PersistenceException(String.format("索引[%s]更新mapping出现异常！[%s]", index.getName(), builder.toString()), e);
		}
		if (!putMappingResponse.isAcknowledged()) {
			throw new PersistenceException(String.format("索引[%s] mapping创建失败！[%s]", index.getName(), builder.toString()));
		}
	}

	public void delete(String indexName) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
		try {
			AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
			if (!deleteIndexResponse.isAcknowledged()) {
				throw new RuntimeException("删除索引[%s]出现失败！");
			}
		} catch (Exception e) {
			if (e instanceof ElasticsearchStatusException && e.getMessage().contains("index_not_found_exception")) {
				logger.error("索引{}已被删除，继续执行操作！", indexName);
			} else {
				throw new RuntimeException(String.format("删除索引[%s]出现异常！", indexName), e);
			}
		}
	}

	@Override
	public void batchInsert(EsIndex index, List<EsIndex> list) {
		throw new UnsupportedOperationException("暂不支持");
	}
}
