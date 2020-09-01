package com.cl.log.server.model;

import com.alibaba.fastjson.JSONObject;
import com.cl.log.config.model.LogFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 业务日志.
 *
 * @author leichu 2020-06-23.
 */
public class BizLog extends BasicAttr implements ILog, Serializable {

	public static final String INDEX_PREFIX = "biz_log-";

	private static final long serialVersionUID = -452660575530931163L;

	private String host;
	private String biz;
	private String traceId;
	private String dateTime;
	private String thread;
	private String level;
	private String clazz;
	private String msg;

	public static void main(String[] args) {
		LocalDateTime time = LocalDateTime.now();
		System.out.println(time);
		System.out.println(time.toString());
		System.out.println(time.getMonth());
		System.out.println(time.getMonthValue());
		System.out.println(time.getDayOfMonth());
	}

	public static BizLog convert(LogFactory.BizLog bzLog) {
		return null;
	}

	public Map<String, ?> convert() {
		Map<String, Object> result = Maps.newHashMap();
		result.put("host", StringUtils.isBlank(this.host) ? "" : this.host);
		result.put("biz", StringUtils.isBlank(this.biz) ? "" : this.biz);
		result.put("traceId", StringUtils.isBlank(this.traceId) ? "" : this.traceId);
		result.put("dateTime", StringUtils.isBlank(this.dateTime) ? "" : this.dateTime);
		result.put("thread", StringUtils.isBlank(this.thread) ? "" : this.thread);
		result.put("level", StringUtils.isBlank(this.level) ? "" : this.level);
		result.put("clazz", StringUtils.isBlank(this.clazz) ? "" : this.clazz);
		result.put("msg", StringUtils.isBlank(this.msg) ? "" : this.msg);

		// 公共属性
		result.put("year", StringUtils.isBlank(this.year) ? "" : this.year);
		result.put("month", StringUtils.isBlank(this.month) ? "" : this.month);
		result.put("day", StringUtils.isBlank(this.day) ? "" : this.day);

		result.put("hour", StringUtils.isBlank(this.hour) ? "" : this.hour);
		result.put("minute", StringUtils.isBlank(this.minute) ? "" : this.minute);
		result.put("second", StringUtils.isBlank(this.second) ? "" : this.second);

		result.put("date4Month", StringUtils.isBlank(this.date4Month) ? "" : this.date4Month);
		result.put("date4Day", StringUtils.isBlank(this.date4Day) ? "" : this.date4Day);
		result.put("date4Hour", StringUtils.isBlank(this.date4Hour) ? "" : this.date4Hour);
		result.put("date4Minute", StringUtils.isBlank(this.date4Minute) ? "" : this.date4Minute);
		result.put("date4Second", StringUtils.isBlank(this.date4Second) ? "" : this.date4Second);

		return result;
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

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
		super.setDateTime(this.dateTime);
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
