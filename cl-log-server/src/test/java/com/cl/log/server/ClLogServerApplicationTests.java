package com.cl.log.server;

import com.cl.log.server.model.EsIndex;
import com.cl.log.server.persistence.IndexRepository;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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
		indexRepository.createIndex(index);
	}

}
