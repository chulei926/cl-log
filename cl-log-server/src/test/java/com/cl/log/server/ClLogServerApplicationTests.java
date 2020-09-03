package com.cl.log.server;

import com.cl.log.server.model.EsIndex;
import com.cl.log.server.persistence.IndexRepository;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class ClLogServerApplicationTests {

	@Resource
	RestHighLevelClient highLevelClient;

	@Resource
	IndexRepository indexRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void esTest() {
		System.out.println(highLevelClient);
		System.out.println(highLevelClient);
	}

	@Test
	void esIndexTest() {
		String path = EsIndex.class.getResource("/").getPath();
		EsIndex index = EsIndex.xml2Index((path.startsWith("/") ? path.substring(1) : path) + "mapping_biz.xml");
		indexRepository.create(index);
	}

	@Test
	public void insertTest() throws IOException {
		BulkRequest request = new BulkRequest();
		request.add(new IndexRequest("posts").id("1").source(XContentType.JSON, "field", "foo"));
		request.add(new IndexRequest("posts").id("2").source(XContentType.JSON, "field", "bar"));
		request.add(new IndexRequest("posts").id("3").source(XContentType.JSON, "field", "baz"));
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		BulkResponse bulkResponse = highLevelClient.bulk(request, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures());

	}

}
