package com.cl.log.server.model;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 基础指标.
 *
 * @author leichu 2020-06-23.
 */
public class BasicAttr implements Serializable {

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
