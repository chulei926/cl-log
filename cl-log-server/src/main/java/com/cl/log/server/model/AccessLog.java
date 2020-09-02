package com.cl.log.server.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Tomcat访问日志.
 *
 * @author leichu 2020-09-01.
 */
public class AccessLog extends BasicAttr implements Serializable {

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

	public Map<String, ?> convert() {
		return null;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBiz() {
		return biz;
	}

	public void setBiz(String biz) {
		this.biz = biz;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
		super.setDateTime(this.dateTime);
	}

	public Integer getStatesCode() {
		return statesCode;
	}

	public void setStatesCode(Integer statesCode) {
		this.statesCode = statesCode;
	}

	public Integer getConsume() {
		return consume;
	}

	public void setConsume(Integer consume) {
		this.consume = consume;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}
}
