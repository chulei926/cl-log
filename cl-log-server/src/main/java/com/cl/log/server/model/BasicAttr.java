package com.cl.log.server.model;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 基础指标.
 *
 * @author leichu 2020-06-23.
 */
public class BasicAttr implements Serializable {

	private static final long serialVersionUID = -3229883569537250806L;

	private String dateTime;
	protected String year;
	protected String month;
	protected String day;
	protected String hour;
	protected String minute;
	protected String second;

	protected String date4Month;  // yyyy-mm
	protected String date4Day;    // yyyy-mm-dd
	protected String date4Hour;   // yyyy-mm-dd HH
	protected String date4Minute; // yyyy-mm-dd HH:mm
	protected String date4Second; // yyyy-mm-dd HH:mm:ss

	public BasicAttr() {
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
		if (StringUtils.isBlank(this.dateTime)) {
			return;
		}
		this.dateTime = this.dateTime.replaceAll("[.]\\d+$", "");
		this.dateTime = this.dateTime.substring(0, 10) + " " + this.dateTime.substring(10);
		final LocalDateTime dt = LocalDateTime.parse(this.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		// 在 真实时间的基础上 减去8小时，写入ES后为真实时间。
		this.dateTime = dt.minusHours(8L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.setYear(String.valueOf(dt.getYear()));
		this.setMonth(String.valueOf(dt.getMonth().getValue()));
		this.setDay(String.valueOf(dt.getDayOfMonth()));
		this.setHour(String.valueOf(dt.getHour()));
		this.setMinute(String.valueOf(dt.getMinute()));
		this.setSecond(String.valueOf(dt.getSecond()));

		this.setDate4Month(dt.format(DateTimeFormatter.ofPattern("yyyy-MM")));
		this.setDate4Day(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		this.setDate4Hour(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH")));
		this.setDate4Minute(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
		this.setDate4Second(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	}

	public void add2Map(Map<String, Object> result) {
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
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getDate4Month() {
		return date4Month;
	}

	public void setDate4Month(String date4Month) {
		this.date4Month = date4Month;
	}

	public String getDate4Day() {
		return date4Day;
	}

	public void setDate4Day(String date4Day) {
		this.date4Day = date4Day;
	}

	public String getDate4Hour() {
		return date4Hour;
	}

	public void setDate4Hour(String date4Hour) {
		this.date4Hour = date4Hour;
	}

	public String getDate4Minute() {
		return date4Minute;
	}

	public void setDate4Minute(String date4Minute) {
		this.date4Minute = date4Minute;
	}

	public String getDate4Second() {
		return date4Second;
	}

	public void setDate4Second(String date4Second) {
		this.date4Second = date4Second;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
