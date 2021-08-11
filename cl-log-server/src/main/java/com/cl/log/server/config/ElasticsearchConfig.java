package com.cl.log.server.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Configuration
public class ElasticsearchConfig {

	private volatile RestHighLevelClient client;

	@Resource
	private ElasticsearchProp elasticsearchProp;

	@Bean
	public RestHighLevelClient restHighLevelClient() {
		if (null == client) {
			synchronized (ElasticsearchConfig.class) {
				if (null == client) {
					client = build();
				}
			}
		}
		return client;
	}

	private RestHighLevelClient build() {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		try {
			List<String> hostAddressList = Splitter.on(",").splitToList(elasticsearchProp.addresses);
			List<HttpHost> hosts = Lists.newArrayList();
			for (String address : hostAddressList) {
				hosts.add(HttpHost.create(address));
			}
			RestClientBuilder restClientBuilder = RestClient.builder(hosts.stream().toArray(HttpHost[]::new));
			client = new RestHighLevelClient(restClientBuilder);
			log.warn("ES客户端初始化完成，ES地址：{}", elasticsearchProp.addresses);
			updateClusterSetting();
			registerHook();
		} catch (Exception e) {
			throw new RuntimeException("ES客户端初始化失败！", e);
		}
		return client;
	}

	private void updateClusterSetting() {
		try {
			ClusterGetSettingsResponse getSettingsResponse = client.cluster().getSettings(new ClusterGetSettingsRequest(), RequestOptions.DEFAULT);
			String maxBuckets = getSettingsResponse.getPersistentSettings().get("search.max_buckets");
			if (StringUtils.isBlank(maxBuckets) || Integer.parseInt(maxBuckets) < 100000) {
				ClusterUpdateSettingsRequest updateSettingsRequest = new ClusterUpdateSettingsRequest();
				Settings persistentSettings = Settings.builder().put("search.max_buckets", 100000).build();
				updateSettingsRequest.persistentSettings(persistentSettings);
				ClusterUpdateSettingsResponse response = client.cluster().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
				if (response.isAcknowledged()) {
					log.warn("ES集群配置[search.max_buckets=100000]更新成功");
				}
			}
		} catch (Exception e) {
			log.error("获取ES集群配置异常！", e);
		}
		log.warn("ES集群配置更新完成......");
	}

	public void close() {
		if (null != client) {
			synchronized (ElasticsearchConfig.class) {
				if (null != client) {
					try {
						client.close();
						client = null;
						log.warn("Elasticsearch客户端连接已关闭！");
					} catch (Exception e) {
						throw new RuntimeException("ES客户端关闭失败！", e);
					}
				}
			}
		}
	}

	private void registerHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(this::close));
	}

	@Data
	@Configuration
	@ConfigurationProperties(prefix = "elasticsearch")
	public static class ElasticsearchProp {
		private String addresses;
		private String numberOfShards;
		private String numberOfReplicas;
		private String maxResultWindow;
	}
}

