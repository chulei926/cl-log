package com.cl.log.config.common;

/**
 * 对象转换异常.
 *
 * @author leichu 2020-06-23.
 */
public class ConvertException extends RuntimeException {
	public ConvertException() {
	}

	public ConvertException(String message) {
		super(message);
	}

	public ConvertException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConvertException(Throwable cause) {
		super(cause);
	}

	public ConvertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
