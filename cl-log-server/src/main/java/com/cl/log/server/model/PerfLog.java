package com.cl.log.server.model;

import com.cl.log.config.model.LogFactory;

import java.io.Serializable;

/**
 * 性能日志.
 *
 * @author leichu 2020-06-23.
 */
public class PerfLog extends BasicAttr implements Serializable {

	public static final String INDEX_PREFIX = "perf_log-";

	private static final long serialVersionUID = 8215972064986858821L;

	public static PerfLog convert(LogFactory.PerfLog perfLog) {
		return null;
	}

}
