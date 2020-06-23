package com.cl.log.server.persistence;

import com.cl.log.config.common.ConvertException;
import com.cl.log.config.common.PersistenceException;
import com.cl.log.server.model.EsIndex;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class IndexRepository extends AbstractRepository<EsIndex> {

	@Resource
	private RestHighLevelClient client;

	@Resource
	Integer numberOfShards;

	@Resource
	Integer numberOfReplicas;

	@Resource
	Integer maxResultWindow;

	@Override
	public void createIndex(EsIndex index) {
		XContentBuilder builder = null;
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

	@Override
	public void batchInsert(EsIndex index, List<EsIndex> list) {
		throw new UnsupportedOperationException("暂不支持");
	}
}
