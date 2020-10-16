package com.cl.log.server;

import com.cl.log.config.utils.DateUtils;
import com.cl.log.server.model.EsIndex;
import com.cl.log.server.persistence.IndexRepository;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ClLogServerApplicationTests {

	static final Logger logger = LoggerFactory.getLogger(ClLogServerApplicationTests.class);

	@Resource
	RestHighLevelClient highLevelClient;

	@Resource
	IndexRepository indexRepository;

	@Resource
	CuratorFramework curatorFramework;

	@Test
	void contextLoads() {
	}

	@Test
	void zkLockTest(){
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 1000; i++) {
			executor.submit(() -> {
				try {
					// 创建锁对象
					InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, "/lock-space");
					// 获取锁
					interProcessMutex.acquire(10, TimeUnit.SECONDS);
					// 如果获取锁成功，则执行对应逻辑
					logger.warn("获取分布式锁，执行对应逻辑1");
					logger.warn("获取分布式锁，执行对应逻辑2");
					logger.warn("获取分布式锁，执行对应逻辑3");
					// 释放锁
					interProcessMutex.release();
				} catch (Exception e) {
					logger.error("", e);
				}
			});
		}
		while (true){
			DateUtils.sleep(30000);
		}
	}

	@Test
	void esTest() {
		System.out.println(highLevelClient);
	}

	@Test
	void esIndexTest() {
//		String path = EsIndex.class.getResource("/").getPath();
//		EsIndex index = EsIndex.xml2Index((path.startsWith("/") ? path.substring(1) : path) + "mapping_biz.xml");
//		indexRepository.create(index);
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
