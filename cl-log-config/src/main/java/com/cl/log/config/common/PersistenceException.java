package com.cl.log.config.common;

/**
 * 持久化异常.
 *
 * @author leichu 2020-06-23.
 */
public class PersistenceException extends RuntimeException {

	public PersistenceException() {
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}

	public PersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
