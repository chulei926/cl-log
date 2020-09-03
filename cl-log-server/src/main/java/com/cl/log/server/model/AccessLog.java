package com.cl.log.server.model;

import com.cl.log.config.model.LogFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Tomcat访问日志.
 *
 * @author leichu 2020-09-01.
 */
public class AccessLog extends BasicAttr implements Serializable {

	public static final String INDEX_PREFIX = "access_log-";

	private static final long serialVersionUID = 3270966166591778160L;

	private String traceId;
	private String host;
	private String biz;
	private Integer port;
	private String userId;
	private String dateTime;
	private Integer statesCode;
	private Integer consume;
	private String ip;
	private String requestMethod;
	private String requestURL;
	private String params;

	public Map<String, Object> convert() {
		Map<String, Object> result = Maps.newHashMap();
		result.put("host", StringUtils.isBlank(this.host) ? "" : this.host);
		result.put("biz", StringUtils.isBlank(this.biz) ? "" : this.biz);
		result.put("traceId", StringUtils.isBlank(this.traceId) ? "" : this.traceId);
		result.put("dateTime", StringUtils.isBlank(this.dateTime) ? "" : this.dateTime);
		result.put("port", this.port);
		result.put("userId", StringUtils.isBlank(this.userId) ? "" : this.userId);
		result.put("statesCode", this.statesCode);
		result.put("consume", this.consume);
		result.put("ip", StringUtils.isBlank(this.ip) ? "" : this.ip);
		result.put("requestMethod", StringUtils.isBlank(this.requestMethod) ? "" : this.requestMethod);
		result.put("requestURL", StringUtils.isBlank(this.requestURL) ? "" : this.requestURL);
		result.put("params", StringUtils.isBlank(this.params) ? "" : this.params);
		// 公共属性
		add2Map(result);
		return result;
	}

	public static AccessLog convert(LogFactory.TomcatAccessLog accessLog) {
		AccessLog log = new AccessLog();
		log.setHost(accessLog.getHost())
				.setTraceId(accessLog.getTraceId())
				.setBiz(accessLog.getBiz())
				.setPort(accessLog.getPort())
				.setUserId(accessLog.getUserId())
				.setStatesCode(StringUtils.isNotBlank(accessLog.getStatesCode()) ? Integer.parseInt(accessLog.getStatesCode()) : -1)
				.setConsume(accessLog.getConsume())
				.setIp(accessLog.getIp())
				.setRequestMethod(accessLog.getRequestMethod())
				.setRequestURL(accessLog.getRequestURL())
				.setParams(accessLog.getParams())
				.setDateTime(accessLog.getDateTime());
		return log;
	}

	public String getTraceId() {
		return traceId;
	}

	public AccessLog setTraceId(String traceId) {
		this.traceId = traceId;
		return this;
	}

	public String getHost() {
		return host;
	}

	public AccessLog setHost(String host) {
		this.host = host;
		return this;
	}

	public String getBiz() {
		return biz;
	}

	public AccessLog setBiz(String biz) {
		this.biz = biz;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public AccessLog setPort(Integer port) {
		this.port = port;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public AccessLog setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		super.setDateTime(dateTime);
		this.dateTime = super.getDateTime();
	}

	public Integer getStatesCode() {
		return statesCode;
	}

	public AccessLog setStatesCode(Integer statesCode) {
		this.statesCode = statesCode;
		return this;
	}

	public Integer getConsume() {
		return consume;
	}

	public AccessLog setConsume(Integer consume) {
		this.consume = consume;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public AccessLog setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public AccessLog setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
		return this;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public AccessLog setRequestURL(String requestURL) {
		this.requestURL = requestURL;
		return this;
	}

	public String getParams() {
		return params;
	}

	public AccessLog setParams(String params) {
		this.params = params;
		return this;
	}
}
