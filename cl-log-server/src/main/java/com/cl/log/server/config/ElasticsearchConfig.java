package com.cl.log.server.config;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import javax.annotation.Resource;

@Configuration
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

	private static final int NUMBER_OF_SHARDS = 3;
	private static final int NUMBER_OF_REPLICAS = 0;
	private static final int MAX_RESULT_WINDOW = 100000000;

	@Resource
	private Environment environment;

	@Bean
	public String[] esHost() {
		String esAddress = environment.getProperty("es.addresses");
		if (StringUtils.isBlank(esAddress)) {
			throw new RuntimeException("请先配置 elasticsearch 集群地址！");
		}
		return esAddress.split(",");
	}

	@Bean
	public Integer numberOfShards() {
		String shards = environment.getProperty("es.index.number_of_shards");
		if (StringUtils.isBlank(shards)) {
			return NUMBER_OF_SHARDS;
		}
		return Integer.parseInt(shards);
	}

	@Bean
	public Integer numberOfReplicas() {
		String replicas = environment.getProperty("es.index.number_of_replicas");
		if (StringUtils.isBlank(replicas)) {
			return NUMBER_OF_REPLICAS;
		}
		return Integer.parseInt(replicas);
	}

	@Bean
	public Integer maxResultWindow() {
		String max = environment.getProperty("es.index.max_result_window");
		if (StringUtils.isBlank(max)) {
			return MAX_RESULT_WINDOW;
		}
		return Integer.parseInt(max);
	}


	@Override
	@Bean
	public RestHighLevelClient elasticsearchClient() {

		final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
				.connectedTo(esHost())
				.build();
		return RestClients.create(clientConfiguration).rest();
	}


}
